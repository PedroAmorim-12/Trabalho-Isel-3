package org.example

import pt.isel.canvas.Canvas
import pt.isel.canvas.WHITE


data class Racket(
    val center: Position,
    val width: Int = 60,
    val height: Int = 10
) {
    companion object
}



val Racket.left: Int get() = center.x - width / 2
val Racket.right: Int get() = center.x + width / 2
val Racket.top: Int get() = center.y - height / 2
val Racket.bottom: Int get() = center.y + height / 2

fun Racket.draw(arena: Canvas) {
    arena.drawRect(left, top, width, height, WHITE)
}
fun Racket.moveToMouse(mouseX: Int, areaWidth: Int): Racket {
    val newX = mouseX
        .coerceIn(width / 2, areaWidth - width / 2)

    return this.copy(center = Position(newX, center.y))
}

fun Racket.Companion.initial(arena: Canvas): Racket =
    Racket(
        center = Position(
            x = arena.width / 2,
            y = arena.height - 30
        )
    )
fun Racket.computeNewDx(hitX: Int, oldDx: Int): Int {
    val localX = hitX - left

    val extreme = 10
    val inner = width - 2 * extreme
    val mild = inner / 4            // 10
    val centerBand = inner - 2 * mild // 20

    val z1 = extreme
    val z2 = z1 + mild
    val z3 = z2 + centerBand
    val z4 = z3 + mild

    val newDx = when {
        localX < 0 || localX >= width -> oldDx
        localX < z1 -> oldDx - 3
        localX < z2 -> oldDx - 1
        localX < z3 -> oldDx
        localX < z4 -> oldDx + 1
        else -> oldDx + 3
    }

    return newDx.coerceIn(DX_MIN, DX_MAX)
}
