package org.example

import pt.isel.canvas.*

data class Area(val width: Int, val height: Int)

data class Game(
    val area: Area,
    val ball: Ball,
    val racket: Racket,
    val bricks: List<Brick>,
    val score: Int,
    val spareBalls: Int,
    val launched: Boolean,
    val gameOver: Boolean
) {
    companion object
}

fun Game.Companion.initial(arena: Canvas): Game {
    val area = Area(arena.width, arena.height)
    val racket = Racket.initial(arena)
    val ball = Ball.onRacket(racket)
    val bricks = initialBricks()
    return Game(
        area = area,
        ball = ball,
        racket = racket,
        bricks = bricks,
        score = 0,
        spareBalls = SPARE_BALLS_INITIAL,
        launched = false,
        gameOver = false
    )
}

fun initialBricks(): List<Brick> {
    val all = mutableListOf<Brick>()

    val leftCols  = 1..3
    val midCols   = 5..7
    val rightCols = 9..11
    val blocks = listOf(leftCols, midCols, rightCols)
    for (row in BRICK_ROWS_COLORS.indices) {
        val color = BRICK_ROWS_COLORS[row]
        for (cols in blocks) {
            for (col in cols) {
                all += normalBrickAt(col, row, color)
            }
        }
    }
    val centralCol = 6
    val topRow = 0
    val last = BRICK_ROWS_COLORS.lastIndex
    val bottomRows = listOf(last - 2, last - 1, last)

    val centralX = normalBrickAt(centralCol, topRow, BRICK_ROWS_COLORS[topRow]).left
    val specialsY = buildSet {
        add(TOP_GAP_PX + topRow * BRICK_H)
        bottomRows.forEach { r -> add(TOP_GAP_PX + r * BRICK_H) }
    }
    val filtered = all.filterNot { it.left == centralX && it.top in specialsY }

    val specials = buildList {
        add(goldBrickAt(centralCol, topRow))
        bottomRows.forEach { r -> add(silverBrickAt(centralCol, r)) }
    }

    return filtered + specials
}


fun Game.moveRacketTo(mouseX: Int): Game {
    if (gameOver) return this
    val newRacket = racket.moveToMouse(mouseX, area.width)
    val newBall = if (!launched) Ball.onRacket(newRacket, ball.color) else ball
    return copy(racket = newRacket, ball = newBall)
}

fun Game.launchBall(): Game {
    if (gameOver) return this
    if (launched) return this
    return copy(ball = ball.launchFromRacket(), launched = true)
}

fun Game.tick(): Game {
    if (gameOver) return this
    if (!launched) {
        return copy(ball = Ball.onRacket(racket, ball.color))
    }

    var currentBall = ball.move()

    currentBall = currentBall.bounceIn(area)
    currentBall = currentBall.reflectOnRacketIfNeeded(racket)

    val brickHit = currentBall.firstBrickCollision(bricks)
    val (afterBrickBall, afterBricks, gained) = if (brickHit == null) {
        Triple(currentBall, bricks, 0)
    } else {
        val (bouncedBall, newBricks, points) = resolveBrickCollision(currentBall, brickHit, bricks)
        Triple(bouncedBall, newBricks, points)
    }

    if (afterBrickBall.center.y - afterBrickBall.radius > area.height) {
        return onBallLost(afterBricks, score + gained)
    }

    val newScore = score + gained

    if (afterBricks.none { !it.indestructible }) {
        return endGame(newScore, spareBalls)
    }

    return copy(ball = afterBrickBall, bricks = afterBricks, score = newScore)
}

private fun Game.onBallLost(currentBricks: List<Brick>, newScore: Int): Game {
    // If no more spare balls, game over
    return if (spareBalls <= 0) {
        endGame(newScore, 0).copy(bricks = currentBricks)
    } else {
        val nextSpare = spareBalls - 1
        copy(
            ball = Ball.onRacket(racket),
            bricks = currentBricks,
            score = newScore,
            spareBalls = nextSpare,
            launched = false
        )
    }
}

private fun Game.endGame(currentScore: Int, spare: Int): Game {
    val finalScore = currentScore + spare * BONUS_PER_SPARE_BALL
    return copy(score = finalScore, launched = false, gameOver = true)
}

fun Ball.bounceIn(area: Area): Ball {
    val r = radius

    val leftLimit = r
    val rightLimit = area.width - r
    val topLimit = r

    var x = center.x
    var y = center.y
    var dx = dir.dx
    var dy = dir.dy

    if (x < leftLimit) {
        x = leftLimit
        dx = -dx
    } else if (x > rightLimit) {
        x = rightLimit
        dx = -dx
    }

    if (y < topLimit) {
        y = topLimit
        dy = -dy
    }

    return copy(center = Position(x, y), dir = Direction(dx, dy))
}

private fun Ball.reflectOnRacketIfNeeded(racket: Racket): Ball {
    val withinX = center.x in racket.left..racket.right
    val touchingY =
        center.y + radius >= racket.top &&
            center.y - radius <= racket.bottom &&
            dir.dy > 0

    return if (withinX && touchingY) {
        reflectOn(racket)
    } else {
        this
    }
}

private fun Ball.firstBrickCollision(bricks: List<Brick>): Brick? =
    bricks.firstOrNull { b ->
        right >= b.left &&
                left <= b.right &&
                bottom >= b.top &&
                top <= b.bottom
    }
private fun resolveBrickCollision(ball: Ball, hit: Brick, all: List<Brick>): Triple<Ball, List<Brick>, Int> {
    val overlapLeft = ball.right - hit.left
    val overlapRight = hit.right - ball.left
    val overlapTop = ball.bottom - hit.top
    val overlapBottom = hit.bottom - ball.top

    val minOverlapX = minOf(overlapLeft, overlapRight)
    val minOverlapY = minOf(overlapTop, overlapBottom)

    val newDir = if (minOverlapX < minOverlapY) {
        Direction(-ball.dir.dx, ball.dir.dy)
    } else {
        Direction(ball.dir.dx, -ball.dir.dy)
    }


    val bounced = ball.copy(dir = newDir)

    val nudged = bounced.copy(
        center = Position(
            bounced.center.x + bounced.dir.dx,
            bounced.center.y + bounced.dir.dy
        )
    )
    if (hit.indestructible) {
        return Triple(nudged, all, 0)
    }

    val remainingHits = hit.hitsLeft - 1
    val newBricks = if (remainingHits <= 0) {
        all.filterNot { it == hit }
    } else {
        all.map { if (it == hit) it.copy(hitsLeft = remainingHits) else it }
    }

    val gained = if (remainingHits <= 0) hit.points else 0

    return Triple(nudged, newBricks, gained)
}

fun Game.draw(arena: Canvas) {
    arena.drawRect(0, 0, area.width, area.height, BLACK)

    bricks.forEach { it.draw(arena) }
    racket.draw(arena)
    ball.draw(arena)

    val fontSize = 16
    arena.drawText(10, 20, "Pontos: $score", WHITE, fontSize)
    arena.drawText(10, 40, "Bolas suplentes: $spareBalls", WHITE, fontSize)

    if (gameOver) {
        val msg = "GAME OVER"
        val size = 24
        val x = area.width / 2 - (msg.length * size) / 4
        val y = area.height / 2
        arena.drawText(x, y, msg, WHITE, size)

        val msg2 = "Final: $score"
        val size2 = 18
        val x2 = area.width / 2 - (msg2.length * size2) / 4
        arena.drawText(x2, y + 30, msg2, WHITE, size2)
    }
}


val Ball.left get() = center.x - radius
val Ball.right get() = center.x + radius
val Ball.top get() = center.y - radius
val Ball.bottom get() = center.y + radius