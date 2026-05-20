package org.example

import pt.isel.canvas.*


const val GOLD = 0xFFD700
const val GRAY = 0xC0C0C0

const val BRICK_W = 32
const val BRICK_H = 15
const val ARENA_COLS = 13
const val ARENA_W = ARENA_COLS * BRICK_W
const val ARENA_H = 600

const val TOP_GAP_PX = BRICK_H * 3

const val SPARE_BALLS_INITIAL = 5
const val BONUS_PER_SPARE_BALL = 10

val BRICK_POINTS: Map<Int, Int> = mapOf(
    WHITE to 1,
    CYAN to 3,
    GREEN to 4,
    RED to 6,
    BLUE to 7,
    MAGENTA to 8,
    YELLOW to 9
)

val BRICK_ROWS_COLORS: List<Int> = listOf(
    WHITE,
    CYAN,
    GREEN,
    RED,
    BLUE,
    MAGENTA,
    YELLOW
)
const val SILVER_HITS = 2
