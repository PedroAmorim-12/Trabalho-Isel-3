package org.example

import pt.isel.canvas.BLACK
import pt.isel.canvas.Canvas

data class Brick(
    val left: Int,
    val top: Int,
    val width: Int = BRICK_W,
    val height: Int = BRICK_H,
    val color: Int,
    val hitsLeft: Int,
    val points: Int,
    val indestructible: Boolean = false
)
const val BRICK_GAP = 1

private fun colToX(col: Int): Int {
    val gapsBefore = when {
        col <= 3 -> 0
        col <= 6 -> 1
        col <= 9 -> 2
        else     -> 3
    }
    return col * BRICK_W + gapsBefore * BRICK_W
}

private fun pos(col: Int, row: Int): Pair<Int, Int> =
    colToX(col) to (TOP_GAP_PX + row * BRICK_H)

val Brick.right: Int get() = left + width
val Brick.bottom: Int get() = top + height

fun Brick.draw(arena: Canvas) {
    arena.drawRect(left, top, width - BRICK_GAP, height - BRICK_GAP, color)
}

fun normalBrickAt(col: Int, row: Int, color: Int): Brick {
    val (x, y) = pos(col, row)

    val pts = BRICK_POINTS[color] ?: 0
    return Brick(
        left = x,
        top = y,
        color = color,
        hitsLeft = 1,
        points = pts,
        indestructible = false
    )
}

fun silverBrickAt(col: Int, row: Int): Brick {
    val (x, y) = pos(col, row)
    return Brick(
        left = x,
        top = y,
        color = GRAY,
        hitsLeft = SILVER_HITS,
        points = 0,
        indestructible = false
    )
}

fun goldBrickAt(col: Int, row: Int): Brick {
    val (x, y) = pos(col, row)
    return Brick(
        left = x,
        top = y,
        color = GOLD,
        hitsLeft = Int.MAX_VALUE,
        points = 0,
        indestructible = true
    )
}
