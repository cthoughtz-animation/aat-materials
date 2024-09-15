package com.raywenderlich.cinematic.details

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.animation.OvershootInterpolator
import androidx.appcompat.content.res.AppCompatResources
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import com.raywenderlich.cinematic.R
import com.raywenderlich.cinematic.databinding.ViewFavoriteButtonBinding
import com.raywenderlich.cinematic.util.DisplayMetricsUtil

class FavoriteButton @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null, defStyle: Int = 0
) : ConstraintLayout(context, attrs, defStyle) {

    private val TAG = "FavoriteButton"

    private val binding: ViewFavoriteButtonBinding =
        ViewFavoriteButtonBinding.inflate(LayoutInflater.from(context), this)
    private val animators = mutableListOf<ValueAnimator>()

    init {
        layoutParams = LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )

        val padding = DisplayMetricsUtil.dpToPx(16)
        setPadding(padding, 0, padding, padding)
    }

    fun setOnFavoriteClickListener(listener: () -> Unit) {
        binding.favoriteButton.setOnClickListener {
            listener.invoke()
        }
    }

    fun setFavorite(isFavorite: Boolean) {
        binding.favoriteButton.apply {
            icon = if (isFavorite) {
                AppCompatResources.getDrawable(context, R.drawable.ic_baseline_favorite_24)
            } else {
                AppCompatResources.getDrawable(
                    context,
                    R.drawable.ic_baseline_favorite_border_24
                )
            }
            text = if (isFavorite) {
                context.getString(R.string.remove_from_favorites)
            } else {
                context.getString(R.string.add_to_favorites)
            }
        }

        hideProgress()
    }

    fun showProgress() {
        binding.progressBar.isVisible = true
        binding.favoriteButton.apply {
            icon = null
            text = null

            isClickable = false
            isFocusable = false

        }

        animateButton()
    }

    private fun animateButton() {
        /**
         * Set the initialWidth to the measured width of the button and the finalWidth to the
         * measured height. You want the button to animate from its initial width to a final state
         * where it becomes a circle. To convert a rectangle to a square, you need to make the
         * width and height the same. By that same logic, since the button already has rounded
         * corners, making the width and height the same makes it a circle.
         * */
        val initialWidth = binding.favoriteButton.measuredWidth
        val finalWidth = binding.favoriteButton.measuredHeight
        val initialTextSize = binding.favoriteButton.textSize

        Log.d(TAG, "animateButton: initialWidth = $initialWidth")
        Log.d(TAG, "animateButton: finalWidth = $finalWidth")

        /**
         * Instantiate a ValueAnimator using the static of Int, then pass the initialWidth and
         * finalWidth to it
         *
         * The first parameter in this case is the original size that is drown the second value is
         * the size after the animation is complete.
         * */
        val widthAnimator = ValueAnimator.ofInt(initialWidth,finalWidth)

        val alphaAnimator = ObjectAnimator.ofFloat(binding.progressBar,"alpha",0f,1f)

        /**
         * You just created a valueAnimator using the static ofFloat, then passed it the
         * initialTextSize and a final text size value of 0.
         * */
        val textSizeAnimator = ValueAnimator.ofFloat(initialTextSize,0f)

        /**
         * Assign a 1,000 millisecond duration to the animator
         * */
        widthAnimator.duration = 1000
        alphaAnimator.duration = 1000

        /**
         * You've added an OvershootInterpolator to the animator and given it a 1,000 millisecond
         * duration.
         * */
        textSizeAnimator.apply {
            interpolator = OvershootInterpolator()
            duration = 1000
        }

        /**
         * Add an UpdateListener to the animator and assign the animatedValue as the width of the
         * button
         * */
        widthAnimator.addUpdateListener {
            binding.favoriteButton.updateLayoutParams {
                this.width = it.animatedValue as Int
            }
        }
        alphaAnimator.addUpdateListener {
            binding.progressBar.alpha = it.animatedValue as Float
        }

        /**
         * This code assigned an updateListener to the animator and updated the text size of the
         * button using animatedValue. Since the text size needs to be an sp value, you have to
         * divide the animated value by the screen density.
         * */
        textSizeAnimator.addUpdateListener {
            binding.favoriteButton.textSize = (it.animatedValue as Float) / resources
                .displayMetrics.density
        }

        binding.progressBar.apply {
            alpha = 0f
            isVisible = true
        }

        animators.addAll(listOf(widthAnimator,alphaAnimator,textSizeAnimator))

        /**
         * Finally, you start the animation
         * */
        widthAnimator.start()

        alphaAnimator.start()

        textSizeAnimator.start()
    }

    private fun reverseAnimation() {
        /**
         * You loop over the animations list one by one
         * */
        animators.forEach { animation ->
            /**
             * Then, you call reverse on each animation. Yep! It's that easy, just a single function
             * call.
             * */
            animation.reverse()
            /**
             * ONce all the animations are reversed, you clear out the list to keep it tidy and to
             * avoid adding duplicate references to it the next time the animation triggers
             * */
            if (animators.indexOf(animation) == animators.lastIndex) {
                animators.clear()
            }
        }
    }


    private fun hideProgress() {
        binding.progressBar.isVisible = false
        binding.favoriteButton.apply {
            extend()
            isClickable = true
            isFocusable = true
        }

        reverseAnimation()
    }

}