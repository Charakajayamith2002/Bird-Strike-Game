package com.example.minigame

import android.content.Context
import android.content.SharedPreferences
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.view.MotionEvent
import android.view.View

class GameView(var c: Context, var gameTask: GameTask) : View(c) {
    private var myPaint: Paint? = null
    private var speed = 1
    private var time = 0
    private var score = 0
    private var bird = ArrayList<HashMap<String, Any>>()

    var viewWidth = 0
    var viewHeight = 0
    var myPlanePosition = 0

    private val preferences: SharedPreferences = c.getSharedPreferences("GamePreferences", Context.MODE_PRIVATE)

    init {
        myPaint = Paint()
    }

    fun resetGameState() {
        bird.clear()
        score = 0
        speed = 1
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        viewWidth = this.measuredWidth
        viewHeight = this.measuredHeight

        // Generate other planes randomly
        if (time % 700 < 10 + speed) {
            val map = HashMap<String, Any>()
            map["lane"] = (0..2).random()
            map["startTime"] = time
            bird.add(map)
        }

        // Update game time
        time += 10 + speed

        // Set up drawing properties
        myPaint!!.style = Paint.Style.FILL

        // Draw the player's plane
        val planeWidth = viewWidth / 5
        val planeHeight = planeWidth + 10

        val d = resources.getDrawable(R.drawable.whiteplane, null)
        d.setBounds(
            myPlanePosition * viewWidth / 3 + viewWidth / 15 + 25,
            viewHeight - 2 - planeHeight,
            myPlanePosition * viewWidth / 3 + viewWidth / 15 + planeWidth - 25,
            viewHeight - 2
        )
        d.draw(canvas!!)
        myPaint!!.color = Color.GREEN
        var highScore = getHighScore()

        for (i in bird.indices) {
            try {
                val planeX = bird[i]["lane"] as Int * viewWidth / 3 + viewWidth / 15
                val planeY = time - bird[i]["startTime"] as Int
                val d2 = resources.getDrawable(R.drawable.biard, null)

                d2.setBounds(
                    planeX + 25, planeY - planeHeight, planeX + planeWidth - 25, planeY
                )
                d2.draw(canvas)
                if (bird[i]["lane"] as Int == myPlanePosition) {
                    if (planeY > viewHeight - 2 - planeHeight && planeY < viewHeight - 2) {
                        gameTask.closeGame(score)
                    }
                }
                if (planeY > viewHeight + planeHeight) {
                    bird.removeAt(i)
                    score++
                    speed = 1 + Math.abs(score / 8)
                    if (score > highScore) {
                        highScore = score
                        saveHighScore(highScore) // Update high score in SharedPreferences
                    }
                }

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        myPaint!!.color = Color.BLACK
        myPaint!!.textSize = 40f

        canvas.drawText("Score : $score", 80f, 80f, myPaint!!)
        canvas.drawText("High Score : $highScore", 80f, 140f, myPaint!!)
        canvas.drawText("Speed : $speed", 380f, 80f, myPaint!!)
        invalidate()
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        when (event!!.action) {
            MotionEvent.ACTION_DOWN -> {
                val x1 = event.x
                if (x1 < viewWidth / 2) {
                    if (myPlanePosition > 0) {
                        myPlanePosition--
                    }
                }
                if (x1 > viewWidth / 2) {
                    if (myPlanePosition < 2) {
                        myPlanePosition++
                    }
                }
                invalidate() // Redraw the view after updating plane position
            }
            MotionEvent.ACTION_UP -> {

            }
        }
        return true
    }

    private fun saveHighScore(score: Int) {
        preferences.edit().putInt("HighScore", score).apply()
    }

    private fun getHighScore(): Int {
        return preferences.getInt("HighScore", 0)
    }
}
