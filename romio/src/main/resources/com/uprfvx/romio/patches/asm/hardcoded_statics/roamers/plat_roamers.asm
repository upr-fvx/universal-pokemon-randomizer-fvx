    .nds
    .thumb
    .open "pokeplatinum.bin", "pokeplatinum_roamers.bin", 0x02000000

    NEW_INITIALIZE_ROAMER_SUBR_HOOK equ 0x206C438
    OLD_INITIALIZE_ROAMER_SUBR_CONTINUED equ 0x206C47E
    NEW_SET_FLAG_FOR_ROAMER_SUBR_HOOK equ 0x206B68A
    OLD_SET_FLAG_FOR_ROAMER_SUBR_CONTINUED equ 0x206B6D4

    SET_FLAG_FOR_MESPRIT equ 0x206B648
    SET_FLAG_FOR_CRESSELIA equ 0x206B638
    SET_FLAG_FOR_ARTICUNO equ 0x206B678
    SET_FLAG_FOR_ZAPDOS equ 0x206B668
    SET_FLAG_FOR_MOLTRES equ 0x206B658

    ITCM_SRC_START equ 0x02101D20
    ITCM_DEST_START equ 0x01FF8000
    ITCM_OLD_SIZE equ 0x750 ; Originally 0x660, but this occurs after the 0xF0 sized music patch

    NEW_INITIALIZE_ROAMER_SUBR_SIZE equ 0x58
    NEW_INITIALIZE_ROAMER_SUBR equ ITCM_SRC_START + ITCM_OLD_SIZE
    NEW_INITIALIZE_ROAMER_SUBR_ITCM equ ITCM_DEST_START + ITCM_OLD_SIZE
    NEW_SET_FLAG_FOR_ROAMER_SUBR equ ITCM_SRC_START + ITCM_OLD_SIZE + NEW_INITIALIZE_ROAMER_SUBR_SIZE
    NEW_SET_FLAG_FOR_ROAMER_SUBR_ITCM equ ITCM_DEST_START + ITCM_OLD_SIZE + NEW_INITIALIZE_ROAMER_SUBR_SIZE
    BL_OFFSET equ (NEW_INITIALIZE_ROAMER_SUBR) - (NEW_INITIALIZE_ROAMER_SUBR_ITCM)

    ; Hook that jumps to our new InitializeRoamer subroutine
    .org    NEW_INITIALIZE_ROAMER_SUBR_HOOK
    bl      org() + 6
    b       OLD_INITIALIZE_ROAMER_SUBR_CONTINUED
    ldr     r5,=#(NEW_INITIALIZE_ROAMER_SUBR_ITCM + 1)
    bx      r5
    .pool

    ; New subroutine for initializing the roamers
    ; r0 is the parameter for which roamer to initialize
    ; This is not a "proper" subroutine, since it just sets values in r4 and r5 knowing that
    ; the original routine will use them. However, doing this "properly" would be really
    ; annoying, so this will suffice.
    .org NEW_INITIALIZE_ROAMER_SUBR
    .area 88

    push    { lr }
    cmp     r0, #0x0
    beq     @@mesprit
    cmp     r0, #0x1
    beq     @@cresselia
    cmp     r0, #0x2
    beq     @@darkrai
    cmp     r0, #0x3
    beq     @@moltres
    cmp     r0, #0x4
    beq     @@zapdos
    b       @@articuno
@@mesprit:
    ldr     r4,=#0x1E1   ; Mesprit
    mov     r5, 0x32     ; Level 50
    b       @@end
@@cresselia:
    ldr     r4,=#0x1E8   ; Cresselia
    mov     r5, 0x32     ; Level 50
    b       @@end
@@darkrai:
    ldr     r4,=#0x1EB   ; Darkrai (unused)
    mov     r5, 0x28     ; Level 40
    b       @@end
@@moltres:
    ldr     r4,=#0x92    ; Moltres
    mov     r5, 0x3C     ; Level 60
    b       @@end
@@zapdos:
    ldr     r4,=#0x91    ; Zapdos
    mov     r5, 0x3C     ; Level 60
    b       @@end
@@articuno:
    ldr     r4,=#0x90    ; Articuno
    mov     r5, 0x3C     ; Level 60
    b       @@end
@@end:
    pop     { pc }
    .pool
    .endarea

    ; Hook that jumps to our new SetFlagForRoamer subroutine
    .org    NEW_SET_FLAG_FOR_ROAMER_SUBR_HOOK
    bl      org() + 6
    b       OLD_SET_FLAG_FOR_ROAMER_SUBR_CONTINUED
    ldr     r3,=#(NEW_SET_FLAG_FOR_ROAMER_SUBR_ITCM + 1)
    bx      r3
    .pool

    ; New subroutine for setting the status flag for roamers
    ; The original function is called after every battle with a roamer to set their current status
    ; It uses the same three parameters as the original function: r1 is the species ID, r2 is the
    ; current status, and r0 is a pointer to some structure that gets passed.
    .org NEW_SET_FLAG_FOR_ROAMER_SUBR
    .area 96
    
    push    { lr }
    ldr     r3,=#0x1E1   ; Mesprit
    cmp     r1, r3
    beq     @@mesprit
    ldr     r3,=#0x1E8   ; Cresselia
    cmp     r1, r3
    beq     @@cresselia
    ldr     r3,=#0x92    ; Moltres
    cmp     r1, r3
    beq     @@moltres
    ldr     r3,=#0x91    ; Zapdos
    cmp     r1, r3
    beq     @@zapdos
    ldr     r3,=#0x90    ; Articuno
    cmp     r1, r3
    beq     @@articuno
    b       @@end
@@mesprit:
    mov     r1, r2
    bl      BL_OFFSET + SET_FLAG_FOR_MESPRIT
    b       @@end
@@cresselia:
    mov     r1, r2
    bl      BL_OFFSET + SET_FLAG_FOR_CRESSELIA
    b       @@end
@@moltres:
    mov     r1, r2
    bl      BL_OFFSET + SET_FLAG_FOR_MOLTRES
    b       @@end
@@zapdos:
    mov     r1, r2
    bl      BL_OFFSET + SET_FLAG_FOR_ZAPDOS
    b       @@end
@@articuno:
    mov     r1, r2
    bl      BL_OFFSET + SET_FLAG_FOR_ARTICUNO
    b       @@end
@@end:
    pop     { pc }
    .pool
    .endarea

    .close