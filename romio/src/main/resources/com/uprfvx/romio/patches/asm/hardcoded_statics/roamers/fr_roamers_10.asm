    .gba
    .thumb
    .open "pokefirered.gba", "pokefirered_roamers.gba", 0x08000000

    CREATE_INITIAL_ROAMER_MON_HOOK_ADDR equ 0x08141C9E
    CREATE_INITIAL_ROAMER_MON_CONTINUED equ 0x08141CBA
    NEW_SUBR_ADDR equ 0x08A80200

    ; Hook that jumps to our new subroutine
    .org CREATE_INITIAL_ROAMER_MON_HOOK_ADDR
    bl      org() + 8
    mov     r6, r0
    b       CREATE_INITIAL_ROAMER_MON_CONTINUED
    ldr     r2,=#(NEW_SUBR_ADDR + 1)
    bx      r2
    .pool

    ; New subroutine that pc-relative loads Raikou/Entei/Suicune's IDs into r0.
    ; Takes the ID of Bulbasaur/Charmander/Squirtle as input via r0.
    ; We had to write this because the compiler optimized these IDs down to a byte.
    .org NEW_SUBR_ADDR
    .area 40

    push    { r7, lr }
    mov     r1, r0
    cmp     r1, #0x1
    beq     @@bulbasaur
    cmp     r1, #0x4
    beq     @@charmander
    b       @@squirtle
@@bulbasaur:
    ldr     r0,=#0xF4              ; Entei
    b       @@end
@@charmander:
    ldr     r0,=#0xF5              ; Suicune
    b       @@end
@@squirtle:
    ldr     r0,=#0xF3              ; Raikou
@@end:
    pop     { r7, pc }
    .pool
    .endarea

    .close