# UPR-10 — Java→C# Porting Notes: Semantic Hazards and AI Workflow

| Field             | Value                                                                                    |
|-------------------|------------------------------------------------------------------------------------------|
| Status            | Active reference                                                                         |
| Date              | 2026-05-29                                                                               |
| Author/Model      | claude-sonnet-4-6 (Claude Code agent, UPR worker, story UPR-10)                         |
| Repo              | https://github.com/TimvanderWal504/universal-pokemon-randomizer-fvx                     |
| Branch            | upr/UPR-10                                                                               |
| Describes port at | `da5dfa5b` — commit on `upr/UPR-9` that added `DSDecmp.cs` + `DSDecmpDifferentialTests.cs` |
| Target language   | C# (.NET 10) — see UPR-7 for ADR                                                        |
| Related issues    | UPR-2 (parent epic), UPR-7 (target-language ADR), UPR-8 (test harness), UPR-9 (DSDecmp port), UPR-10 (this document) |
| Documentation hub | https://timvanderwal504.atlassian.net/wiki/spaces/AM/pages/26050562                     |

---

## Context

UPR-9 produced the first real Java→C# class translation: `DSDecmp`, a DS ROM
LZ10/LZ11 decompressor.  The Java source lives at
`utils/src/main/java/compressors/DSDecmp.java`; the C# port is at
`port/src/UniversalPokemonRandomizer.Core/Compressors/DSDecmp.cs` (commit
`da5dfa5b`).

This document captures the semantic hazards encountered, the AI-assisted
workflow that produced the port, and a reusable checklist for every subsequent
porting story (Phase 2 onward).  It is the primary cross-phase fallback: an
agent starting Phase 2 cold should read this first.

---

## Semantic Hazards

### 1. Unsigned-Byte Masking (`& 0xFF`)

**What it is.**
Java's `byte` type is *signed*: values -128..127.  When a `byte` is read from
an array and used in an arithmetic expression, the JVM zero-extends it into an
`int` *as a signed value*, meaning `0xFF` stored in a `byte[]` produces `-1`
in an expression, not `255`.  Java programmers guard against this with
`data[i] & 0xFF`, which masks away sign-extension bits to yield 0..255.

C#'s `byte` type is *unsigned*: values 0..255.  Reading `data[i]` directly
already yields 0..255; the `& 0xFF` guard is *not required* for the read
itself.

**Java before:**
```java
int flags = data[offset++] & 0xFF;
int b = data[offset++] & 0xFF;
```

**C# after:**
```csharp
int flags = data[offset++] & 0xFF;   // & 0xFF is harmless but unnecessary
int b = data[offset++];               // equivalent; byte is already 0..255
```

The UPR-9 port kept `& 0xFF` on reads involving arithmetic intermediates (such
as the flag byte and back-reference bytes) for clarity and to ensure the
intermediate stays typed as `int` rather than narrowing to `byte`.  Both forms
are correct in C#; the key is that the result is used as `int`.

**What breaks if mishandled.**
If a C# port *omits* the intermediate `int` and directly assigns back to
`byte`, the arithmetic truncates.  For example:

```csharp
// BUG: Literal byte stored directly, no issue here — but if arithmetic is done
// on the byte type:
byte b = data[offset++];
int n = (b >> 4) + 3;   // OK: b promotes to int for the shift
byte nibble = (byte)(b & 0x0F);  // OK as byte, but if shifted further, truncation risk
```

The real hazard is forgetting the `int` intermediate when combining nibbles across
bytes (see Hazard 2 below).

**Fixture that would catch it.**
Any of the 8 LZ10 or 9 LZ11 golden-vector fixtures in
`DSDecmp_LZ10_DifferentialTests` / `DSDecmp_LZ11_DifferentialTests`.  A byte
value >= 0x80 in a back-reference token would produce the wrong length or
displacement, corrupting output from that point onward.  The first fixture
containing a high-bit byte in a flag token would fail with a mismatched output
length or corrupt content.

---

### 2. Sign Extension in Back-Reference Arithmetic (Nibble Recombination)

**What it is.**
Both LZ10 and LZ11 encode back-reference length and displacement by splitting
them across *nibbles* — 4-bit halves of bytes.  Extracting the high nibble
(`b >> 4`) and low nibble (`b & 0x0F`) and then recombining them across byte
boundaries requires the intermediate values to stay as `int`.

In Java, because `byte` is signed and the `& 0xFF` guard already promotes
the read to `int`, sign extension is already neutralised at the point of the
read.  The nibble operations then work on clean ints.

In C#, if you ever allow an intermediate to narrow back to `byte` before the
shift/OR recombination, sign extension is *not* the issue (C# byte is
unsigned) but *bit truncation* is: `(byte)(b1 << 4)` would silently discard
the top bits of `b1`.

**Java before (LZ11 case-0, 3-byte encoding):**
```java
int b1 = data[offset++] & 0xFF;
int bt = data[offset++] & 0xFF;
len  = b1 << 4;     // b1's low nibble becomes the high nibble of len
len |= bt >> 4;     // bt's high nibble becomes the low nibble of len
len += 0x11;
```

**C# after:**
```csharp
int b1 = data[offset++] & 0xFF;  // or just data[offset++]; both yield int
int bt = data[offset++] & 0xFF;
int len = (b1 << 4) | (bt >> 4);
len += 0x11;
```

**What breaks if mishandled.**
If `b1` were stored as a `byte` before the left shift, `b1 << 4` would still
produce a widened `int` in C# (the shift operator promotes to `int`), so the
immediate result is correct.  However, if the assignment were
`byte len = (byte)(b1 << 4)`, the top bits would be truncated to 8 bits before
the OR with `bt >> 4`, producing a length that is at most 255 instead of the
correct 12-bit-ish value.  The decompressor would then emit far fewer bytes in
the back-reference copy loop, yielding a truncated output that fails all
golden-vector checks for LZ11 case-0 tokens.

**Fixture that would catch it.**
`DSDecmp_LZ11_DifferentialTests.DecompressLZ11_MatchesGoldenVector` — any of
the 9 LZ11 golden vectors that contain a case-0 or case-1 back-reference token
(high nibble 0x0 or 0x1).  The output length mismatch or corrupt byte sequence
would be detected by `DifferentialAssert.BytesEqual`.

---

### 3. Integer Overflow in Decompressed-Size Calculation (3-Byte LE Header)

**What it is.**
Both LZ10 and LZ11 headers begin with one type byte followed by a 3-byte
little-endian decompressed length.  Java reads it as:

```java
int length = (data[offset] & 0xFF)
           | ((data[offset + 1] & 0xFF) << 8)
           | ((data[offset + 2] & 0xFF) << 16);
```

In Java, each `data[...] & 0xFF` call yields an `int` in the range 0..255, and
the shifts/ORs produce a non-negative 24-bit value (0..16 777 215).  No
overflow is possible because Java `int` is 32-bit signed.

In C#, `byte` is already unsigned, so `& 0xFF` is unnecessary but harmless.
The arithmetic is identical; `int` is also 32-bit signed and the 24-bit result
is always positive.

**Java before:**
```java
int length = (data[offset] & 0xFF)
           | ((data[offset + 1] & 0xFF) << 8)
           | ((data[offset + 2] & 0xFF) << 16);
```

**C# after:**
```csharp
int length = data[offset]
           | (data[offset + 1] << 8)
           | (data[offset + 2] << 16);
```

Both are correct.  The UPR-9 port kept the `& 0xFF` form (matching the Java
source verbatim) for traceability.

**What breaks if mishandled.**
The only risk is if the shift were applied to a signed type wider than `int`
(e.g., `long`) and the byte's high bit were set — then sign extension before the
shift would set high bits.  In practice this does not occur for `byte`→`int`
promotion in C# because `byte` is unsigned.  The extended-header fallback
(`length == 0` → read 4-byte big-endian) uses a helper `ReadFullIntBigEndian`
that must also apply `& 0xFF` per byte to avoid signed promotion of the
individual bytes before their shifts — both Java and C# do this correctly.

**Fixture that would catch it.**
Any golden vector whose compressed header encodes a length whose high bit of any
header byte is set (i.e., byte >= 0x80).  A wrong length would cause
`new byte[length]` to allocate the wrong buffer, producing either an
`IndexOutOfRangeException` or a zero-padded short output.  All 17 golden vectors
exercise this path; the first one with a high-bit header byte would catch it.

---

### 4. LZ11 Extended-Length Codes (Nibble-Mask on Signed vs. Unsigned Bytes)

**What it is.**
LZ11 has three back-reference encoding widths, dispatched on the *high nibble*
of the first back-reference byte (`b1 >> 4`):

| High nibble | Width   | Encoding  |
|-------------|---------|-----------|
| `0x0`       | 3 bytes | `len = (b1 << 4 \| bt >> 4) + 0x11` |
| `0x1`       | 4 bytes | `len = ((b1 & 0xF) << 12 \| bt << 4 \| b2 >> 4) + 0x111` |
| `0x2..0xF`  | 2 bytes | `len = (b1 >> 4) + 1` |

The dispatch `b1 >> 4` is the key: if `b1` is a Java signed byte with value
>= 0x80, then `b1 & 0xFF` (already done at read time) promotes it to a
positive int; `b1 >> 4` then gives 8..15 as expected.

In C#, `data[offset++]` already yields an unsigned `byte` which, when used
in the right-shift expression, is promoted to `int` with the high bit as 0.
`b1 >> 4` gives 0..15 correctly.

**The hazard** is if a C# port *skips* the `int` intermediate and reads
directly into a `byte` variable, then performs `b1 >> 4`:

```csharp
// POTENTIAL BUG: b1 as byte, then shift
byte b1raw = data[offset++];
int dispatch = b1raw >> 4;  // C# promotes byte to int; result is correct 0..15
```

This is actually safe in C# because `byte >> int` promotes `byte` to `int`
before shifting.  The real risk in Java would be if someone tried to reuse the
raw byte without `& 0xFF`; that risk does not exist in C#.

**Java before:**
```java
int b1 = data[offset++] & 0xFF;
switch (b1 >> 4) {
    case 0:  /* 3-byte */
    case 1:  /* 4-byte */
    default: /* 2-byte */
}
```

**C# after:**
```csharp
int b1 = data[offset++] & 0xFF;  // equivalent: int b1 = data[offset++];
switch (b1 >> 4)
{
    case 0:  /* 3-byte */
    case 1:  /* 4-byte */
    default: /* 2-byte */
}
```

**What breaks if mishandled.**
If the `& 0xFF` guard were *missing in Java* and `b1` were raw signed byte,
values 0x80..0xFF would give `b1 >> 4` as -8..-1 (arithmetic right-shift of a
negative value fills with 1-bits).  The `switch` default case would never match
`case 0` or `case 1` for those tokens, but the negative dispatch value would
fall to the `default` and the decompressor would produce a wrong length.  In
C# this path cannot happen because `byte` is unsigned; however, if a port
copied Java code *without* adding `& 0xFF* and used a `sbyte` type instead of
`byte`, the bug would re-appear.

**Fixture that would catch it.**
`DSDecmp_LZ11_DifferentialTests` — any of the 9 LZ11 fixtures containing a
3-byte or 4-byte extended-length token (high nibble 0x0 or 0x1).  A wrong
dispatch would produce an incorrect `len` value, copying the wrong number of
back-reference bytes, corrupting all subsequent output.

---

### 5. Off-by-One in Inner Loop Guard (`>` vs `>=`)

**What it is.**
This is not a Java/C# *type* difference but a logic bug present in the Java
source that was fixed during porting.  The Java code uses:

```java
if (curr_size > outData.length)
    break;
```

The correct guard is `>=` because when `curr_size == outData.length` the buffer
is exactly full and no more bytes should be written.  Using `>` means one
extra loop iteration executes, which in Java would throw
`ArrayIndexOutOfBoundsException` for the write but *not* for the outer `while`
condition — the outer while loop would re-enter with `curr_size == length`,
consume more of the compressed stream, and likely throw or corrupt data.

**Java before (bug):**
```java
if (curr_size > outData.length)
    break;
```

**C# after (fixed):**
```csharp
if (currSize >= outData.Length)
    break;
```

The C# port also changed the outer `while` loop guard from
`curr_size < outData.length` (identical in both languages) to the same form,
ensuring consistency.

**What breaks if mishandled.**
With the `>` guard, the decompressor overwrites one byte past the allocated
buffer when the last flag bit in a flag byte resolves to a literal.  In Java
this raises `ArrayIndexOutOfBoundsException` on the write; in C# it would
raise `IndexOutOfRangeException`.  If the runtime exception were swallowed (as
it is in the Java source with a `catch (ArrayIndexOutOfBoundsException)` block
around the literal write), the symptom would instead be a silently truncated
output.

**Fixture that would catch it.**
All 17 golden-vector fixtures exercise the end-of-stream logic.  Any fixture
where the last flag byte has extra set bits beyond the final data would trigger
the over-run.  The differential assertion `DifferentialAssert.BytesEqual`
would catch a length mismatch immediately.

---

### 6. Endianness of the Extended-Length Header (Big-Endian 4-Byte Fallback)

**What it is.**
When the 3-byte LE length field is zero, both LZ10 and LZ11 read a 4-byte
big-endian signed integer via `IOFunctions.readFullIntBigEndian`.  Java and C#
do not have a built-in "big-endian int" read in their standard byte-array APIs
(Java's `DataInputStream.readInt()` is big-endian, but the source uses a
hand-rolled helper; C#'s `BinaryReader` defaults to little-endian).

The helper must manually shift and OR the bytes in big-endian order:

```java
// Java: IOFunctions.readFullIntBigEndian
return (data[offset] & 0xFF) << 24
     | (data[offset+1] & 0xFF) << 16
     | (data[offset+2] & 0xFF) << 8
     |  (data[offset+3] & 0xFF);
```

```csharp
// C#: inlined private helper ReadFullIntBigEndian
private static int ReadFullIntBigEndian(byte[] data, int offset)
    => ((data[offset] & 0xFF) << 24)
     | ((data[offset + 1] & 0xFF) << 16)
     | ((data[offset + 2] & 0xFF) << 8)
     |  (data[offset + 3] & 0xFF);
```

The `& 0xFF` on each byte is technically unnecessary in C# (byte is unsigned)
but is harmless and makes the port traceable line-for-line back to the Java
source.

**What breaks if mishandled.**
If someone used `BinaryReader.ReadInt32()` instead of the hand-rolled helper,
the bytes would be read little-endian, producing a completely wrong length for
compressed streams that use the extended header.  This would cause
`new byte[length]` to allocate the wrong size, and the decompressor would
either run off the end of the input array or return a wrongly sized output.

**Fixture that would catch it.**
A golden vector whose compressed header has a zero 3-byte length field (i.e.,
uses the extended 4-byte header).  If such a fixture exists in the 17 cases
captured by UPR-8, it would catch the endianness bug immediately.  If no
current fixture exercises this path, it is an open question (see section below).

---

## AI Workflow

### Prompting strategy that worked

The UPR-9 agent was given:
1. The exact Java source file to port.
2. An explicit list of semantic hazards to address (the unsigned-byte masking,
   sign extension, integer overflow, nibble mask, and off-by-one).
3. The existing test harness structure (UPR-8) to understand how to wire up
   differential tests.
4. The UPR-7 ADR for target framework and project layout.

The agent produced a first-pass C# file that was structurally correct and
translated all three LZ11 encoding cases.  The key prompt elements that made
this work:
- "Handle unsigned-byte arithmetic via int intermediates throughout"
- "Fix the Java inner-loop off-by-one: `curr_size >= outData.Length` not `>`"
- "Keep `& 0xFF` on every byte read that feeds arithmetic, for traceability"

### Systematic mistakes the agent made

1. **Retaining `& 0xFF` verbatim from Java** — while not incorrect, the agent
   initially left `& 0xFF` on every read including the literal-byte path in
   LZ11's `else` branch.  The final port drops it there (`outData[currSize++] =
   data[offset++]`) to show that no masking is needed when storing directly into
   a `byte[]`.  This was a minor style inconsistency, not a functional bug.

2. **Method visibility** — the agent initially made `DecompressLZ10` and
   `DecompressLZ11` private (matching the Java `private static` methods).
   The orchestrator's review caught that the differential tests call these
   methods directly by name, requiring `public` access.

3. **Comment accuracy on the off-by-one** — the initial code comment said "Java
   inner-loop bug `curr_size > outData.length` (should be `>=`)" which was
   correct, but the comment in the code block was initially placed on the wrong
   `break` statement.  Manual review of the diff caught this.

### Review steps that caught bugs

1. **Line-by-line diff against Java source** — every `& 0xFF` in Java was
   traced to its C# equivalent to confirm no masking was silently dropped.
2. **Case-by-case LZ11 switch verification** — each of the three cases (`0`,
   `1`, `default`) was checked against the byte-layout comments in the Java
   source to confirm nibble shift directions were preserved.
3. **Method signature check** — confirmed public visibility for all methods
   the test class references.
4. **Golden-vector pass** — the 17 differential tests (`dotnet test port/`)
   all passed byte-for-byte, confirming the port is semantically identical
   to the Java original on the captured inputs.

---

## Phase 2 Checklist / Prompt Preamble

The following block is a verbatim, copy-pasteable prompt fragment to prepend
when asking an agent to port any Java class to C# in this project.

---

```
## Java→C# Porting Constraints — Universal Pokemon Randomizer

You are porting a Java class to C# (.NET 10). Apply these rules on every line:

### Byte arithmetic
- Java `byte` is SIGNED (-128..127). Java source uses `data[i] & 0xFF` to get
  0..255. C# `byte` is UNSIGNED (0..255), so `& 0xFF` is NOT required for reads,
  but keep it on every read that feeds arithmetic (for traceability and to
  ensure int promotion).
- NEVER store an arithmetic intermediate back into a `byte` variable before a
  shift or OR. Always use `int` for nibble, flag, length, and displacement
  intermediates.

### Shift and nibble operations
- `b >> 4` (high nibble) and `b & 0x0F` (low nibble) MUST be done on an `int`,
  not a `byte`. In C#, `byte >> int` promotes byte to int automatically, but
  be explicit: always store reads as `int b = data[offset++] & 0xFF;` before
  shifting.
- When recombining nibbles across bytes (e.g., `len = (b1 << 4) | (bt >> 4)`),
  confirm the result is a `int` all the way through; never truncate to `byte`
  mid-calculation.

### Integer overflow
- The 3-byte LE decompressed-size header (`data[1] | data[2] << 8 | data[3] << 16`)
  is a non-negative 24-bit value; `int` is sufficient. No `long` needed.
- The 4-byte big-endian fallback header MUST be read with a hand-rolled helper
  (shift + OR per byte). Do NOT use `BinaryReader.ReadInt32()` — it reads
  little-endian.

### Off-by-one at end of buffer
- Java source uses `curr_size > outData.length` as the inner-loop break guard.
  This is a BUG. The correct guard is `curr_size >= outData.Length` (or
  equivalently, the outer while condition `currSize < outData.Length` serves
  as the primary guard). Fix this proactively.

### Method visibility
- If the test file calls a method directly, that method MUST be `public`.
  Check the test class before deciding on `private` vs `public`.

### Naming
- Follow C# conventions: PascalCase for methods and properties, camelCase for
  locals. `curr_size` → `currSize`, `outData` → `outData` (already OK),
  `cdest` → `cdest` or `copyDest`.

### Verification
- After porting, run: `dotnet test port/ --no-restore`
- All differential tests (golden-vector fixtures) must pass byte-for-byte.
  Any failure indicates a semantic mismatch — do not ignore or suppress.

### Related issues
- UPR-7: target language ADR (C# .NET 10, xUnit, project layout)
- UPR-8: differential test harness and golden-vector fixture corpus
- UPR-9: first real port (DSDecmp) — the reference implementation
- UPR-10: this porting-notes document (semantic hazards detail)
```

---

## Open Questions

1. **Extended-header coverage** — None of the 17 golden vectors captured in
   UPR-8 is known to exercise the `length == 0` branch (the 4-byte big-endian
   fallback header).  If Phase 2 ports another compressor that uses extended
   headers, a specific fixture should be added to cover this path.  The current
   test suite does not validate the `ReadFullIntBigEndian` helper end-to-end
   for non-trivial lengths.

2. **Unported functions** — The Java `DSDecmp` file contains only two functions
   (`decompress10LZ`, `decompress11LZ`) plus the dispatch `Decompress`.  All
   three are ported.  However, `IOFunctions.readFullIntBigEndian` was inlined
   into the C# port rather than placed in a shared utility class.  Phase 2 may
   want to extract it if other ported classes also need big-endian reads.

3. **Compression (encoding) not ported** — The Java randomizer also uses LZ
   compression (encoding) in other parts of the codebase.  The UPR-9 scope was
   decompression only.  Phase 2 stories that port a compressor class will need
   to revisit endianness handling on the *write* side, where C# `BinaryWriter`
   defaults to little-endian and big-endian writes require manual byte ordering.

4. **`IOFunctions` class** — The Java `IOFunctions.readFullIntBigEndian` helper
   referenced by `DSDecmp` has not been ported as a standalone utility class.
   Phase 2 should either inline it (as UPR-9 did) or create a
   `port/src/.../Utils/IOFunctions.cs` shared class if more than 2-3 callers
   emerge.

5. **`sbyte` risk** — C# has an `sbyte` type (signed byte, -128..127) that
   mirrors Java's `byte`.  If any agent uses `sbyte` thinking it maps to Java
   `byte`, all the sign-extension hazards reappear.  The Phase 2 preamble
   above prohibits this implicitly, but should be stated explicitly in code
   review: reject any `sbyte` in ported code.

---

## Resume Point

An agent picking up Phase 2 porting work after this document should:

1. Check out the branch for the story being worked on (pattern: `upr/UPR-NN`).
2. Confirm the test suite is green: `dotnet restore port/ --configfile port/NuGet.config && dotnet test port/ --no-restore`.
   Expected baseline (as of UPR-9): Passed 17+, Failed 0.
3. Read the Java source class to be ported.  For each `byte` read, note whether
   it feeds arithmetic — if so, apply the `int` intermediate rule from the
   checklist above.
4. Paste the **Phase 2 checklist / prompt preamble** (see above) into your
   working context before generating the C# port.
5. After generating, perform a line-by-line diff of every back-reference length
   and displacement calculation against the Java source.
6. Run `dotnet test port/ --no-restore` and confirm all golden-vector tests
   pass before committing.
7. The reference port is `port/src/UniversalPokemonRandomizer.Core/Compressors/DSDecmp.cs`
   (commit `da5dfa5b` on branch `upr/UPR-9`).  Use it as the structural template
   for decompressor classes.
