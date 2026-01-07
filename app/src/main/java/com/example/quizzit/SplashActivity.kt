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
import com.example.quizzit.utils.PreferenceManager

class SplashActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySplashBinding
    private val splashDuration = 3000L // 3 seconds

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            binding = ActivitySplashBinding.inflate(layoutInflater)
            setContentView(binding.root)

            // Initialize PreferenceManager
            PreferenceManager.init(this)

            // Hide action bar for full screen splash
            supportActionBar?.hide()

            // Start animations safely
            try {
                animateSplash()
            } catch (e: Exception) {
                e.printStackTrace()
                // If animations fail, just navigate
                navigateAfterDelay()
            }

            // Navigate after splash duration
            Handler(Looper.getMainLooper()).postDelayed({
                checkLoginStatus()
            }, splashDuration)

        } catch (e: Exception) {
            e.printStackTrace()
            // If anything fails, navigate to login
            checkLoginStatus()
        }
    }

    private fun navigateAfterDelay() {
        Handler(Looper.getMainLooper()).postDelayed({
            checkLoginStatus()
        }, 1000)
    }

    private fun checkLoginStatus() {
        try {
            if (PreferenceManager.isLoggedIn()) {
                // User is already logged in, go to MainActivity
                val intent = Intent(this, MainActivity::class.java)
                intent.putExtra("USERNAME", PreferenceManager.getUsername())
                startActivity(intent)
            } else {
                // User is not logged in, go to LoginCredentials
                startActivity(Intent(this, LoginCredentials::class.java))
            }
            finish()
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        } catch (e: Exception) {
            e.printStackTrace()
            // Default to login if anything goes wrong
            startActivity(Intent(this, LoginCredentials::class.java))
            finish()
        }
    }

    private fun animateSplash() {
        try {
            // Animate Logo Card - Scale and Fade In
            if (::binding.isInitialized && binding.logoCard != null) {
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
            }

            // Animate App Name - Slide Up and Fade In
            if (::binding.isInitialized && binding.appName != null) {
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
            }

            // Animate Tagline - Fade In
            if (::binding.isInitialized && binding.tagline != null) {
                binding.tagline.apply {
                    alpha = 0f

                    animate()
                        .alpha(0.95f)
                        .setDuration(600)
                        .setStartDelay(800)
                        .start()
                }
            }

            // Animate Loading Dots
            if (::binding.isInitialized) {
                animateLoadingDots()
                animateBackgroundCircles()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun animateLoadingDots() {
        try {
            val dots = listOf(binding.dot1, binding.dot2, binding.dot3)

            dots.forEachIndexed { index, dot ->
                if (dot != null) {
                    dot.alpha = 0f
                    dot.scaleX = 0f
                    dot.scaleY = 0f

                    Handler(Looper.getMainLooper()).postDelayed({
                        animateDotBounce(dot)
                    }, 1200L + (index * 150L))
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun animateDotBounce(dot: View) {
        try {
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
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun animateBackgroundCircles() {
        try {
            if (binding.circle1 != null) {
                binding.circle1.animate()
                    .rotation(360f)
                    .setDuration(20000)
                    .setInterpolator(AccelerateDecelerateInterpolator())
                    .withEndAction {
                        binding.circle1.rotation = 0f
                        animateBackgroundCircles()
                    }
                    .start()
            }

            if (binding.circle2 != null) {
                binding.circle2.animate()
                    .rotation(-360f)
                    .setDuration(25000)
                    .setInterpolator(AccelerateDecelerateInterpolator())
                    .start()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}