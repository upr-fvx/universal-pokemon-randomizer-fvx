    .gba
    .thumb
    .open "pkmnleafgreen.gba", "pkmnleafgreen_musicfix.gba", 0x08000000
    .include "frlg_musicfix_constants.asm"

    START_LEGENDARY_BATTLE_SWITCH_HOOK_ADDR equ (START_LEGENDARY_BATTLE_SWITCH_HOOK_BASE_ADDR + LG_10_OFFSET)
    START_LEGENDARY_BATTLE_MEWTWO_CASE equ (START_LEGENDARY_BATTLE_MEWTWO_CASE_BASE_ADDR + LG_10_OFFSET)
    START_LEGENDARY_BATTLE_DEOXYS_CASE equ (START_LEGENDARY_BATTLE_DEOXYS_CASE_BASE_ADDR + LG_10_OFFSET)
    START_LEGENDARY_BATTLE_LEGENDARY_BIRD_CASE equ (START_LEGENDARY_BATTLE_LEGENDARY_BIRD_CASE_BASE_ADDR + LG_10_OFFSET)
    START_LEGENDARY_BATTLE_DEFAULT_CASE equ (START_LEGENDARY_BATTLE_DEFAULT_CASE_BASE_ADDR + LG_10_OFFSET)
    NEW_SUBR_ADDR equ 0x08A80100

    ; Hook that jumps to our new subroutine
    .org START_LEGENDARY_BATTLE_SWITCH_HOOK_ADDR
    bl      org() + 6
    b       START_LEGENDARY_BATTLE_DEFAULT_CASE             ; Failsafe, should never actually be used
    ldr     r2,=#(NEW_SUBR_ADDR + 1)
    bx      r2
    .pool

    ; New subroutine. Checks each relevant index one-by-one and picks the correct music+intro
    .org NEW_SUBR_ADDR
    .area 104

    push    lr
    mov     r2,sp
    ldr     r1,=#0x90   ; Articuno
    cmp     r0,r1
    beq     @@music_legendary_bird
    ldr     r1,=#0x91   ; Zapdos
    cmp     r0,r1
    beq     @@music_legendary_bird
    ldr     r1,=#0x92   ; Moltres
    cmp     r0,r1
    beq     @@music_legendary_bird
    ldr     r1,=#0x96   ; Mewtwo
    cmp     r0,r1
    beq     @@music_mewtwo
    ldr     r1,=#0xF9   ; Lugia
    cmp     r0,r1
    beq     @@music_legendary_bird
    ldr     r1,=#0xFA   ; Ho-oh
    cmp     r0,r1
    beq     @@music_legendary_bird
    ldr     r1,=#0x19A  ; Deoxys
    cmp     r0,r1
    beq     @@music_deoxys
@@music_mewtwo:
    ldr     r1,=#START_LEGENDARY_BATTLE_MEWTWO_CASE+1
    b       @@subr_end
@@music_legendary_bird:
    ldr     r1,=#START_LEGENDARY_BATTLE_LEGENDARY_BIRD_CASE+1
    b       @@subr_end
@@music_deoxys:
    ldr     r1,=#START_LEGENDARY_BATTLE_DEOXYS_CASE+1
@@subr_end:
    str     r1,[r2]
    pop     r0
    bx      r0
    .pool
    .endarea

    .close
