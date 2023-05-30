package com.example.vonageapp.view.fragment

import android.Manifest
import android.opengl.GLSurfaceView
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.vonageapp.databinding.FragmentVonageBinding
import com.example.vonageapp.view.util.OpenTokConfig
import com.opentok.android.BaseVideoRenderer
import com.opentok.android.OpentokError
import com.opentok.android.Publisher
import com.opentok.android.PublisherKit
import com.opentok.android.PublisherKit.PublisherListener
import com.opentok.android.Session
import com.opentok.android.Stream
import com.opentok.android.Subscriber
import com.opentok.android.SubscriberKit
import com.opentok.android.SubscriberKit.SubscriberListener
import dagger.hilt.android.AndroidEntryPoint
import pub.devrel.easypermissions.AfterPermissionGranted
import pub.devrel.easypermissions.EasyPermissions


@AndroidEntryPoint
class VonageFragment : Fragment() {
    private var binding: FragmentVonageBinding? = null
    var session: Session? = null
    private var publisher: Publisher? = null
    private var subscriber: Subscriber? = null
    private val TAG = VonageFragment::class.java.simpleName
    val PERMISSIONS_REQUEST_CODE = 124


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestPermissions()

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentVonageBinding.inflate(inflater, container, false)

        // Inflate the layout for this fragment
        return binding!!.root
    }
    @AfterPermissionGranted(124)
    private fun requestPermissions() {
        val perms = arrayOf<String>(
            Manifest.permission.INTERNET,
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO
        )
        if (EasyPermissions.hasPermissions(requireContext(), *perms)) {
            // initialize and connect to the session
            initializeSession(OpenTokConfig.API_KEY, OpenTokConfig.SESSION_ID, OpenTokConfig.TOKEN);
        } else {
            EasyPermissions.requestPermissions(
                this,
                "This app needs access to your camera and mic to make video calls",
                PERMISSIONS_REQUEST_CODE,
                *perms
            )
        }
    }
    private fun initializeSession(apiKey: String, sessionId: String, token: String) {
        Log.i(TAG, "apiKey: $apiKey")
        Log.i(TAG, "sessionId: $sessionId")
        Log.i(TAG, "token: $token")
        session = Session.Builder(requireContext(), apiKey, sessionId).build()
        session?.setSessionListener(sessionListener)
        session?.connect(token)

    }
    private val publisherListener: PublisherListener = object : PublisherListener {
        override fun onStreamCreated(publisherKit: PublisherKit, stream: Stream) {
            Log.d(TAG, "onStreamCreated: Publisher Stream Created. Own stream " + stream.streamId)
        }

        override fun onStreamDestroyed(publisherKit: PublisherKit, stream: Stream) {
            Log.d(
                TAG,
                "onStreamDestroyed: Publisher Stream Destroyed. Own stream " + stream.streamId
            )
        }

        override fun onError(publisherKit: PublisherKit, opentokError: OpentokError) {
            Log.e(TAG, "PublisherKit onError: " + opentokError.message)
        }
    }
    fun onStreamReceived(session: Session, stream: Stream) {
        Log.d(
            TAG,
            "onStreamReceived: New Stream Received " + stream.streamId + " in session: " + session.sessionId
        )
        if (subscriber == null) {
            subscriber = Subscriber.Builder(requireContext(), stream).build()
            subscriber?.renderer?.setStyle(BaseVideoRenderer.STYLE_VIDEO_SCALE, BaseVideoRenderer.STYLE_VIDEO_FILL)
            subscriber?.setSubscriberListener(subscriberListener)
            session.subscribe(subscriber)
            binding?.subscriberContainer?.addView(subscriber?.view)
        }
    }

    private val sessionListener: Session.SessionListener = object : Session.SessionListener {
        override fun onConnected(session: Session) {
            Log.d(TAG, "onConnected: Connected to session: " + session.sessionId)

            publisher = Publisher.Builder(requireContext()).build()
            publisher?.setPublisherListener(publisherListener)
            publisher?.renderer?.setStyle(BaseVideoRenderer.STYLE_VIDEO_SCALE, BaseVideoRenderer.STYLE_VIDEO_FILL)

            binding?.publisherContainer?.addView(publisher?.view)

            if (publisher?.getView() is GLSurfaceView) {
                (publisher?.getView() as GLSurfaceView).setZOrderOnTop(true)
            }

            session.publish(publisher)        }

        override fun onDisconnected(session: Session) {
            Log.d(TAG, "onDisconnected: Disconnected from session: " + session.sessionId)
        }

        override fun onStreamReceived(session: Session, stream: Stream) {
            Log.d(
                TAG,
                "onStreamReceived: New Stream Received " + stream.streamId + " in session: " + session.sessionId
            )

            if (subscriber == null) {
                subscriber = Subscriber.Builder(requireContext(), stream).build()
                subscriber?.renderer?.setStyle(
                    BaseVideoRenderer.STYLE_VIDEO_SCALE,
                    BaseVideoRenderer.STYLE_VIDEO_FILL
                )
                subscriber?.setSubscriberListener(subscriberListener)
                session.subscribe(subscriber)
                binding?.subscriberContainer?.addView(subscriber?.view)
            }
        }

        override fun onStreamDropped(session: Session, stream: Stream) {
            Log.i(TAG, "Stream Dropped");

            if (subscriber != null) {
                subscriber = null;
                binding?.subscriberContainer?.removeAllViews();
            }
        }

        override fun onError(session: Session, opentokError: OpentokError) {
            Log.e(TAG, "Session error: " + opentokError.message)
        }
    }
    override fun onPause() {
        super.onPause()
        if (session != null) {
            session!!.onPause()
        }
    }
    @Deprecated("Deprecated in Java")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
    }
    override fun onResume() {
        super.onResume()
        if (session != null) {
            session!!.onResume()
        }
    }
    var subscriberListener: SubscriberListener = object : SubscriberListener {
        override fun onConnected(subscriberKit: SubscriberKit) {
            Log.d(
                TAG,
                "onConnected: Subscriber connected. Stream: " + subscriberKit.stream.streamId
            )
        }

        override fun onDisconnected(subscriberKit: SubscriberKit) {
            Log.d(
                TAG,
                "onDisconnected: Subscriber disconnected. Stream: " + subscriberKit.stream.streamId
            )
        }

        override fun onError(subscriberKit: SubscriberKit, opentokError: OpentokError) {
            Log.e(TAG, "SubscriberKit onError: " + opentokError.message)
        }
    }
    fun onStreamDropped(session: Session?, stream: Stream?) {
        Log.i(TAG, "Stream Dropped")
        if (subscriber != null) {
            subscriber = null
            binding?.subscriberContainer?.removeAllViews()
        }
    }
}