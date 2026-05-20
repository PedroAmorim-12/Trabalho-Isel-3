package org.example


import pt.isel.canvas.Canvas
import pt.isel.canvas.BLUE

data class Position(val x: Int, val y: Int)
data class Direction(val dx: Int, val dy: Int)


operator fun Position.plus(pos: Position): Position =
    Position(this.x + pos.x, this.y + pos.y)


operator fun Position.plus(dir: Direction): Position =
    Position(this.x + dir.dx, this.y + dir.dy)


data class Ball(
    val center: Position,
    val radius: Int = 7,
    val color: Int,
    val dir: Direction
) {
    companion object
}

const val DX_MIN = -6
const val DX_MAX = 6
const val DY_STEP = 4

fun randomDx(): Int {
    var v: Int
    do {
        v = (DX_MIN..DX_MAX).random()
    } while (v == 0)
    return v
}

fun Ball.Companion.onRacket(racket: Racket, color: Int = BLUE): Ball {
    val r = 7
    val x = racket.center.x
    val y = racket.top - r
    return Ball(
        center = Position(x, y),
        radius = r,
        color = color,
        dir = Direction(0, 0)
    )
}

fun Ball.launchFromRacket(): Ball {
    val dx = randomDx()
    val dy = -DY_STEP
    return this.copy(dir = Direction(dx, dy))
}

fun Ball.draw(arena: Canvas) {
    arena.drawCircle(center.x, center.y, radius, color)
}

fun Ball.move(): Ball =
    Ball(center + dir, radius, color, dir)

fun Ball.reflectOn(racket: Racket): Ball {
    val newDx = racket.computeNewDx(center.x, dir.dx)
    val newDy = -dir.dy
    return this.copy(dir = Direction(newDx, newDy))
}