import org.example.*
import pt.isel.canvas.*


fun main() {
    onStart {
        val arena = Canvas(ARENA_W, ARENA_H, BLACK)

        var game = Game.initial(arena)
        arena.onTimeProgress(10) {
            game = game.tick()
            game.draw(arena)
        }
        arena.onMouseMove { mouse ->
            game = game.moveRacketTo(mouse.x)
        }
        arena.onMouseDown {
            game = game.launchBall()
        }
    }
    onFinish {
        print("Finish ")
    }
    print("End ")
}