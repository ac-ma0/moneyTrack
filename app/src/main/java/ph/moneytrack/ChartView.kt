package ph.moneytrack

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.view.View

class ChartView(context: Context, private val income: Long, private val expense: Long) : View(context) {
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val max = maxOf(income, expense, 1L).toFloat()
        val width = width * .28f
        val base = height - 36f
        paint.textSize = 28f
        val incomeHeight = (height - 70f) * income / max
        val expenseHeight = (height - 70f) * expense / max
        paint.color = Color.rgb(34, 145, 92); canvas.drawRect(width, base - incomeHeight, width * 2, base, paint)
        paint.color = Color.rgb(205, 71, 71); canvas.drawRect(width * 2.5f, base - expenseHeight, width * 3.5f, base, paint)
        paint.color = Color.DKGRAY; paint.textSize = 13f
        canvas.drawText("Income", width, height - 12f, paint); canvas.drawText("Expense", width * 2.35f, height - 12f, paint)
    }
}
