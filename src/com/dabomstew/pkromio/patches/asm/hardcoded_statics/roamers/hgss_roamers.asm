    .nds
    .thumb
    .open "pkmnheartgold.bin", "pkmnheartgold_roamers.bin", 0x02000000

    NEW_INITIALIZE_ROAMER_SUBR_HOOK equ 0x2067708
    OLD_INITIALIZE_ROAMER_SUBR_CONTINUED equ 0x206773E
    NEW_GET_FLAG_OFFSET_FOR_ROAMER_SUBR_HOOK equ 0x20677F6
    OLD_GET_FLAG_OFFSET_FOR_ROAMER_SUBR_CONTINUED equ 0x206782E

    ITCM_SRC_START equ 0x02111860
    ITCM_DEST_START equ 0x01FF8000
    ITCM_OLD_SIZE equ 0x67C ; Originally 0x620, but this occurs after the 0x5C sized catching tutorial patch

    NEW_INITIALIZE_ROAMER_SUBR_SIZE equ 0x3C
    NEW_INITIALIZE_ROAMER_SUBR equ ITCM_SRC_START + ITCM_OLD_SIZE
    NEW_INITIALIZE_ROAMER_SUBR_ITCM equ ITCM_DEST_START + ITCM_OLD_SIZE
    NEW_GET_FLAG_OFFSET_FOR_ROAMER_SUBR equ ITCM_SRC_START + ITCM_OLD_SIZE + NEW_INITIALIZE_ROAMER_SUBR_SIZE
    NEW_GET_FLAG_OFFSET_FOR_ROAMER_SUBR_ITCM equ ITCM_DEST_START + ITCM_OLD_SIZE + NEW_INITIALIZE_ROAMER_SUBR_SIZE

    ; Hook that jumps to our new InitializeRoamer subroutine
    .org    NEW_INITIALIZE_ROAMER_SUBR_HOOK
    bl      org() + 6
    b       OLD_INITIALIZE_ROAMER_SUBR_CONTINUED
    ldr     r6,=#(NEW_INITIALIZE_ROAMER_SUBR_ITCM + 1)
    bx      r6
    .pool

    ; New subroutine for initializing the roamers
    ; r0 is the parameter for which roamer to initialize
    ; This is not a "proper" subroutine, since it just sets values in r5 and r6 knowing that
    ; the original routine will use them. However, doing this "properly" would be really
    ; annoying, so this will suffice.
    .org NEW_INITIALIZE_ROAMER_SUBR
    .area 60

    push    { lr }
    cmp     r0, #0x0
    beq     @@raikou
    cmp     r0, #0x1
    beq     @@entei
    cmp     r0, #0x2
    beq     @@latias
    b       @@latios
@@raikou:
    ldr     r6,=#0xF3    ; Raikou
    mov     r5, 0x28     ; Level 40
    b       @@end
@@entei:
    ldr     r6,=#0xF4    ; Entei
    mov     r5, 0x28     ; Level 40
    b       @@end
@@latias:
    ldr     r6,=#0x17C   ; Latias
    mov     r5, 0x23     ; Level 35
    b       @@end
@@latios:
    ldr     r6,=#0x17D   ; Latios
    mov     r5, 0x23     ; Level 35
    b       @@end
@@end:
    pop     { pc }
    .pool
    .endarea

    ; Hook that jumps to our new GetFlagOffsetForRoamer subroutine
    .org    NEW_GET_FLAG_OFFSET_FOR_ROAMER_SUBR_HOOK
    bl      org() + 6
    b       OLD_GET_FLAG_OFFSET_FOR_ROAMER_SUBR_CONTINUED
    ldr     r2,=#(NEW_GET_FLAG_OFFSET_FOR_ROAMER_SUBR_ITCM + 1)
    bx      r2
    .pool

    ; New subroutine for getting the status flag offset for roamers
    ; The original function is called after every battle with a roamer to set their current status
    ; It has a "base status flag" number, and then it adds the output of this function to the base
    ; in order to get the status flag number for a particular roamer.
    .org NEW_GET_FLAG_OFFSET_FOR_ROAMER_SUBR
    .area 68

    push    { lr }
    ldr     r1,=#0xF3    ; Raikou
    cmp     r0, r1
    beq     @@raikou
    ldr     r1,=#0xF4    ; Entei
    cmp     r0, r1
    beq     @@entei
    ldr     r1,=#0x17C   ; Latias
    cmp     r0, r1
    beq     @@latias
    ldr     r1,=#0x17D   ; Latios
    cmp     r0, r1
    beq     @@latios
    b       @@default
@@raikou:
    mov     r0, #0x0
    b       @@end
@@entei:
    mov     r0, #0x1
    b       @@end
@@latias:
    mov     r0, #0x2
    b       @@end
@@latios:
    mov     r0, #0x3
    b       @@end
@@default:
    mov     r0, #0x4     ; In the original game, it calls the ASSERT function too, but I don't really care to replicate that.
    b       @@end
@@end:
    pop     { pc }
    .pool
    .endarea

    .close