package com.sotopatrick.onjoue.presentation.activity.details

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.painter.Painter
import org.jetbrains.compose.resources.DrawableResource
import onjoue.shared.generated.resources.Res
import onjoue.shared.generated.resources.ic_equipment_ball
import onjoue.shared.generated.resources.ic_equipment_blanket
import onjoue.shared.generated.resources.ic_equipment_blocks
import onjoue.shared.generated.resources.ic_equipment_books
import onjoue.shared.generated.resources.ic_equipment_broom
import onjoue.shared.generated.resources.ic_equipment_brushes
import onjoue.shared.generated.resources.ic_equipment_bubbles
import onjoue.shared.generated.resources.ic_equipment_cardboard
import onjoue.shared.generated.resources.ic_equipment_chairs
import onjoue.shared.generated.resources.ic_equipment_chalk
import onjoue.shared.generated.resources.ic_equipment_clay
import onjoue.shared.generated.resources.ic_equipment_clothespins
import onjoue.shared.generated.resources.ic_equipment_clothing
import onjoue.shared.generated.resources.ic_equipment_colored_paper
import onjoue.shared.generated.resources.ic_equipment_colored_pencils
import onjoue.shared.generated.resources.ic_equipment_containers
import onjoue.shared.generated.resources.ic_equipment_costumes
import onjoue.shared.generated.resources.ic_equipment_craft_foam
import onjoue.shared.generated.resources.ic_equipment_cushions
import onjoue.shared.generated.resources.ic_equipment_empty_bottles
import onjoue.shared.generated.resources.ic_equipment_fabric_squares
import onjoue.shared.generated.resources.ic_equipment_family_photos
import onjoue.shared.generated.resources.ic_equipment_figurines
import onjoue.shared.generated.resources.ic_equipment_food_coloring
import onjoue.shared.generated.resources.ic_equipment_frisbee
import onjoue.shared.generated.resources.ic_equipment_glue
import onjoue.shared.generated.resources.ic_equipment_handkerchiefs
import onjoue.shared.generated.resources.ic_equipment_hook_and_loop
import onjoue.shared.generated.resources.ic_equipment_hoops
import onjoue.shared.generated.resources.ic_equipment_ice_cube_tray
import onjoue.shared.generated.resources.ic_equipment_ice_cubes
import onjoue.shared.generated.resources.ic_equipment_kitchen_tongs
import onjoue.shared.generated.resources.ic_equipment_markers
import onjoue.shared.generated.resources.ic_equipment_metal_ring
import onjoue.shared.generated.resources.ic_equipment_mirror
import onjoue.shared.generated.resources.ic_equipment_paint
import onjoue.shared.generated.resources.ic_equipment_paper
import onjoue.shared.generated.resources.ic_equipment_paper_plates
import onjoue.shared.generated.resources.ic_equipment_paper_towel_roll
import onjoue.shared.generated.resources.ic_equipment_party_balloons
import onjoue.shared.generated.resources.ic_equipment_ping_pong_paddle
import onjoue.shared.generated.resources.ic_equipment_pom_poms
import onjoue.shared.generated.resources.ic_equipment_pool_noodles
import onjoue.shared.generated.resources.ic_equipment_ring_binder
import onjoue.shared.generated.resources.ic_equipment_rope
import onjoue.shared.generated.resources.ic_equipment_scissors
import onjoue.shared.generated.resources.ic_equipment_socks
import onjoue.shared.generated.resources.ic_equipment_soft_toys
import onjoue.shared.generated.resources.ic_equipment_spoons
import onjoue.shared.generated.resources.ic_equipment_spray_bottle
import onjoue.shared.generated.resources.ic_equipment_tape
import onjoue.shared.generated.resources.ic_equipment_textured_materials
import onjoue.shared.generated.resources.ic_equipment_towel
import onjoue.shared.generated.resources.ic_equipment_varnish
import onjoue.shared.generated.resources.ic_equipment_wooden_discs
import onjoue.shared.generated.resources.ic_equipment_wooden_sticks
import onjoue.shared.generated.resources.ic_equipment_yogurt_pot
import org.jetbrains.compose.resources.painterResource

private val equipmentIcons: Map<String, DrawableResource> = mapOf(
    "ic_equipment_ball" to Res.drawable.ic_equipment_ball,
    "ic_equipment_cushions" to Res.drawable.ic_equipment_cushions,
    "ic_equipment_soft_toys" to Res.drawable.ic_equipment_soft_toys,
    "ic_equipment_rope" to Res.drawable.ic_equipment_rope,
    "ic_equipment_chairs" to Res.drawable.ic_equipment_chairs,
    "ic_equipment_chalk" to Res.drawable.ic_equipment_chalk,
    "ic_equipment_tape" to Res.drawable.ic_equipment_tape,
    "ic_equipment_paper" to Res.drawable.ic_equipment_paper,
    "ic_equipment_colored_pencils" to Res.drawable.ic_equipment_colored_pencils,
    "ic_equipment_markers" to Res.drawable.ic_equipment_markers,
    "ic_equipment_scissors" to Res.drawable.ic_equipment_scissors,
    "ic_equipment_glue" to Res.drawable.ic_equipment_glue,
    "ic_equipment_cardboard" to Res.drawable.ic_equipment_cardboard,
    "ic_equipment_clay" to Res.drawable.ic_equipment_clay,
    "ic_equipment_paint" to Res.drawable.ic_equipment_paint,
    "ic_equipment_brushes" to Res.drawable.ic_equipment_brushes,
    "ic_equipment_books" to Res.drawable.ic_equipment_books,
    "ic_equipment_blocks" to Res.drawable.ic_equipment_blocks,
    "ic_equipment_hoops" to Res.drawable.ic_equipment_hoops,
    "ic_equipment_blanket" to Res.drawable.ic_equipment_blanket,
    "ic_equipment_containers" to Res.drawable.ic_equipment_containers,
    "ic_equipment_spoons" to Res.drawable.ic_equipment_spoons,
    "ic_equipment_clothespins" to Res.drawable.ic_equipment_clothespins,
    "ic_equipment_bubbles" to Res.drawable.ic_equipment_bubbles,
    "ic_equipment_pool_noodles" to Res.drawable.ic_equipment_pool_noodles,
    "ic_equipment_costumes" to Res.drawable.ic_equipment_costumes,
    "ic_equipment_socks" to Res.drawable.ic_equipment_socks,
    "ic_equipment_towel" to Res.drawable.ic_equipment_towel,
    "ic_equipment_frisbee" to Res.drawable.ic_equipment_frisbee,
    "ic_equipment_broom" to Res.drawable.ic_equipment_broom,
    "ic_equipment_handkerchiefs" to Res.drawable.ic_equipment_handkerchiefs,
    "ic_equipment_paper_towel_roll" to Res.drawable.ic_equipment_paper_towel_roll,
    "ic_equipment_paper_plates" to Res.drawable.ic_equipment_paper_plates,
    "ic_equipment_ping_pong_paddle" to Res.drawable.ic_equipment_ping_pong_paddle,
    "ic_equipment_party_balloons" to Res.drawable.ic_equipment_party_balloons,
    "ic_equipment_empty_bottles" to Res.drawable.ic_equipment_empty_bottles,
    "ic_equipment_pom_poms" to Res.drawable.ic_equipment_pom_poms,
    "ic_equipment_ice_cube_tray" to Res.drawable.ic_equipment_ice_cube_tray,
    "ic_equipment_kitchen_tongs" to Res.drawable.ic_equipment_kitchen_tongs,
    "ic_equipment_ice_cubes" to Res.drawable.ic_equipment_ice_cubes,
    "ic_equipment_figurines" to Res.drawable.ic_equipment_figurines,
    "ic_equipment_food_coloring" to Res.drawable.ic_equipment_food_coloring,
    "ic_equipment_craft_foam" to Res.drawable.ic_equipment_craft_foam,
    "ic_equipment_spray_bottle" to Res.drawable.ic_equipment_spray_bottle,
    "ic_equipment_wooden_sticks" to Res.drawable.ic_equipment_wooden_sticks,
    "ic_equipment_hook_and_loop" to Res.drawable.ic_equipment_hook_and_loop,
    "ic_equipment_family_photos" to Res.drawable.ic_equipment_family_photos,
    "ic_equipment_colored_paper" to Res.drawable.ic_equipment_colored_paper,
    "ic_equipment_varnish" to Res.drawable.ic_equipment_varnish,
    "ic_equipment_wooden_discs" to Res.drawable.ic_equipment_wooden_discs,
    "ic_equipment_fabric_squares" to Res.drawable.ic_equipment_fabric_squares,
    "ic_equipment_metal_ring" to Res.drawable.ic_equipment_metal_ring,
    "ic_equipment_textured_materials" to Res.drawable.ic_equipment_textured_materials,
    "ic_equipment_yogurt_pot" to Res.drawable.ic_equipment_yogurt_pot,
    "ic_equipment_ring_binder" to Res.drawable.ic_equipment_ring_binder,
    "ic_equipment_mirror" to Res.drawable.ic_equipment_mirror,
    "ic_equipment_clothing" to Res.drawable.ic_equipment_clothing,
)

@Composable
internal fun equipmentIconPainter(iconId: String): Painter? {
    val resource = equipmentIcons[iconId] ?: return null
    return painterResource(resource)
}
