package com.example.quizzit

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.BounceInterpolator
import androidx.appcompat.app.AppCompatActivity
import com.example.quizzit.databinding.ActivitySplashBinding

class SplashActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySplashBinding
    private val splashDuration = 3000L // 3 seconds

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Hide action bar for full screen splash
        supportActionBar?.hide()

        // Start animations
        animateSplash()

        // Navigate to LoginCredentials after splash duration
        Handler(Looper.getMainLooper()).postDelayed({
            startActivity(Intent(this, LoginCredentials::class.java))
            finish()
            // Add fade animation
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }, splashDuration)
    }

    private fun animateSplash() {
        // Animate Logo Card - Scale and Fade In
        binding.logoCard.apply {
            scaleX = 0f
            scaleY = 0f
            alpha = 0f

            animate()
                .scaleX(1f)
                .scaleY(1f)
                .alpha(1f)
                .setDuration(800)
                .setInterpolator(BounceInterpolator())
                .start()
        }

        // Animate App Name - Slide Up and Fade In
        binding.appName.apply {
            translationY = 100f
            alpha = 0f

            animate()
                .translationY(0f)
                .alpha(1f)
                .setDuration(800)
                .setStartDelay(400)
                .setInterpolator(AccelerateDecelerateInterpolator())
                .start()
        }

        // Animate Tagline - Fade In
        binding.tagline.apply {
            alpha = 0f

            animate()
                .alpha(0.95f)
                .setDuration(600)
                .setStartDelay(800)
                .start()
        }

        // Animate Loading Dots - Sequential Bounce
        animateLoadingDots()

        // Animate Background Circles - Rotation
        animateBackgroundCircles()
    }

    private fun animateLoadingDots() {
        val dots = listOf(binding.dot1, binding.dot2, binding.dot3)

        dots.forEachIndexed { index, dot ->
            dot.alpha = 0f
            dot.scaleX = 0f
            dot.scaleY = 0f

            // Animate each dot with delay
            Handler(Looper.getMainLooper()).postDelayed({
                animateDotBounce(dot)
            }, 1200L + (index * 150L))
        }
    }

    private fun animateDotBounce(dot: View) {
        // Create bounce animation that repeats
        val scaleUp = ObjectAnimator.ofFloat(dot, "scaleX", 0f, 1f).apply {
            duration = 300
            interpolator = BounceInterpolator()
        }
        val scaleUpY = ObjectAnimator.ofFloat(dot, "scaleY", 0f, 1f).apply {
            duration = 300
            interpolator = BounceInterpolator()
        }
        val fadeIn = ObjectAnimator.ofFloat(dot, "alpha", 0f, 1f).apply {
            duration = 300
        }

        val initialSet = AnimatorSet().apply {
            playTogether(scaleUp, scaleUpY, fadeIn)
        }

        // Pulsing animation
        val pulseUp = ObjectAnimator.ofFloat(dot, "scaleX", 1f, 1.3f).apply {
            duration = 400
            repeatCount = ObjectAnimator.INFINITE
            repeatMode = ObjectAnimator.REVERSE
        }
        val pulseUpY = ObjectAnimator.ofFloat(dot, "scaleY", 1f, 1.3f).apply {
            duration = 400
            repeatCount = ObjectAnimator.INFINITE
            repeatMode = ObjectAnimator.REVERSE
        }

        initialSet.addListener(object : Animator.AnimatorListener {
            override fun onAnimationStart(animation: Animator) {}
            override fun onAnimationEnd(animation: Animator) {
                pulseUp.start()
                pulseUpY.start()
            }
            override fun onAnimationCancel(animation: Animator) {}
            override fun onAnimationRepeat(animation: Animator) {}
        })

        initialSet.start()
    }

    private fun animateBackgroundCircles() {
        // Rotate first circle slowly
        binding.circle1.animate()
            .rotation(360f)
            .setDuration(20000)
            .setInterpolator(AccelerateDecelerateInterpolator())
            .withEndAction {
                // Repeat rotation
                binding.circle1.rotation = 0f
                animateBackgroundCircles()
            }
            .start()

        // Rotate second circle in opposite direction
        binding.circle2.animate()
            .rotation(-360f)
            .setDuration(25000)
            .setInterpolator(AccelerateDecelerateInterpolator())
            .start()
    }
}