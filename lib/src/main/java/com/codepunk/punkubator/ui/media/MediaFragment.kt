/*
 * Copyright (c) 2018 IGIST.io. All rights reserved.
 * Author(s): Scott Slater
 */

package com.codepunk.punkubator.ui.media

import android.media.MediaPlayer
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import com.codepunk.punkubator.util.PoolingHashMap

/**
 * A retained [Fragment] that maintains a pooled collection of MediaPlayers for smooth media
 * playback across configuration changes.
 */
open class MediaFragment :
    Fragment(),
    LifecycleObserver {

    // region Properties

    /**
     * A collection of pooled [MediaPlayer]s.
     */
    lateinit var mediaPlayers: MediaPlayerPoolingHashMap

    /**
     * A set of keys representing [MediaPlayer]s that were paused when the lifecycle owner
     * stopped so they can be restarted if/when the lifecycle starts again.
     */
    @Suppress("WEAKER_ACCESS")
    protected val pausedPlayers = HashSet<String>()

    // endregion Properties

    // region Lifecycle methods

    /**
     * Sets the retained instance flag and creates the media player collection.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        retainInstance = true
        mediaPlayers =
                MediaPlayerPoolingHashMap(
                    arguments?.getInt(
                        KEY_MAX_POOL_SIZE
                    ) ?: 1
                )
    }

    /**
     * Releases all media players in the collection.
     */
    override fun onDestroy() {
        super.onDestroy()
        for (mediaPlayer in mediaPlayers.values) {
            mediaPlayer.release()
        }
    }

    /**
     * Pauses any currently-playing media players in the collection.
     */
    @Suppress("UNUSED")
    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun onObservedStart() {
        for (key in pausedPlayers) {
            mediaPlayers[key]?.start()
        }
        pausedPlayers.clear()
    }

    /**
     * Re-starts any media players in the collection that were paused during [onObservedStop].
     */
    @Suppress("UNUSED")
    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    fun onObservedStop() {
        for ((tag, mediaPlayer) in mediaPlayers) {
            if (mediaPlayer.isPlaying) {
                mediaPlayer.pause()
                pausedPlayers.add(tag)
            }
        }
    }

    // endregion Lifecycle methods

    // region Methods

    // endregion Methods

    // region Companion object

    companion object {

        // region Properties

        /**
         * A bundle key that sets the desired maximum pool size for [mediaPlayers].
         */
        @JvmStatic
        private val KEY_MAX_POOL_SIZE = "${MediaFragment::class.java.name}.MAX_POOL_SIZE"

        // endregion Properties

        // region Methods

        /**
         * Creates an instance of [MediaFragment] with the desired maximum media player pool size.
         */
        @JvmStatic
        fun newInstance(maxPoolSize: Int = 1): MediaFragment =
            MediaFragment().apply {
                arguments = Bundle().apply {
                    putInt(KEY_MAX_POOL_SIZE, maxPoolSize)
                }
            }

        // endregion Methods

    }

    // endregion Companion object

    // region Nested/inner classes

    /**
     * An implementation of [PoolingHashMap] with some basic logic around the pooling of
     * [MediaPlayer] instances.
     */
    class MediaPlayerPoolingHashMap(maxPoolSize: Int) :
        PoolingHashMap<String, MediaPlayer>(maxPoolSize) {

        // region Inherited methods

        /**
         * Releases the media player's resources when the media player itself is being released
         * into the pool
         */
        override fun onRelease(key: String, value: MediaPlayer) {
            super.onRelease(key, value)
            value.release()
        }

        // endregion Inherited methods

        // region Methods

        /**
         * A version of obtain that supplies a default [MediaPlayer] creation method.
         */
        fun obtain(key: String): MediaPlayer = obtain(key) { MediaPlayer() }

        // endregion Methods

    }

    // endregion Nested/inner classes

}
