    .gba
    .thumb
    .open "pkmnemerald.gba", "pkmnemerald_musicfix.gba", 0x08000000

    START_LEGENDARY_BATTLE_SWITCH_HOOK_ADDR equ 0x080B0952
    START_LEGENDARY_BATTLE_LUGIA_HOOH_CASE equ 0x080B0970
    START_LEGENDARY_BATTLE_GROUDON_CASE equ 0x080B099A
    START_LEGENDARY_BATTLE_KYOGRE_CASE equ 0x080B09B8
    START_LEGENDARY_BATTLE_RAYQUAZA_CASE equ 0x080B09CE
    START_LEGENDARY_BATTLE_DEOXYS_CASE equ 0x080B09E4
    START_LEGENDARY_BATTLE_MEW_CASE equ 0x080B09F4
    NEW_SUBR_ADDR equ 0x08FE0100

    ; Hook that jumps to our new subroutine
    .org START_LEGENDARY_BATTLE_SWITCH_HOOK_ADDR
    bl      org() + 6
    b       START_LEGENDARY_BATTLE_GROUDON_CASE             ; Failsafe, should never actually be used
    ldr     r2,=#(NEW_SUBR_ADDR + 1)
    bx      r2
    .pool

    ; New subroutine. Checks each relevant index one-by-one and picks the correct music+intro
    .org NEW_SUBR_ADDR
    .area 128

    push    lr
    mov     r2,sp
    ldr     r1,=#0x97   ; Mew
    cmp     r0,r1
    beq     @@music_mew
    ldr     r1,=#0xF9   ; Lugia
    cmp     r0,r1
    beq     @@music_lugia_hooh
    ldr     r1,=#0xFA   ; Ho-oh
    cmp     r0,r1
    beq     @@music_lugia_hooh
    ldr     r1,=#0x194  ; Kyogre
    cmp     r0,r1
    beq     @@music_kyogre
    ldr     r1,=#0x195  ; Groudon
    cmp     r0,r1
    beq     @@music_groudon
    ldr     r1,=#0x196  ; Rayquaza
    cmp     r0,r1
    beq     @@music_rayquaza
    ldr     r1,=#0x19A  ; Deoxys
    cmp     r0,r1
    beq     @@music_deoxys
@@music_mew:
    ldr     r1,=#START_LEGENDARY_BATTLE_MEW_CASE+1
    b       @@subr_end
@@music_lugia_hooh:
    ldr     r1,=#START_LEGENDARY_BATTLE_LUGIA_HOOH_CASE+1
    b       @@subr_end
@@music_kyogre:
    ldr     r1,=#START_LEGENDARY_BATTLE_KYOGRE_CASE+1
    b       @@subr_end
@@music_groudon:
    ldr     r1,=#START_LEGENDARY_BATTLE_GROUDON_CASE+1
    b       @@subr_end
@@music_rayquaza:
    ldr     r1,=#START_LEGENDARY_BATTLE_RAYQUAZA_CASE+1
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
