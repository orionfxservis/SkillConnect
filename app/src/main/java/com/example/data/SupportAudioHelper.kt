package com.example.data

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.os.Build
import java.io.File
import java.util.UUID
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

data class AudioRecorderState(
  val isRecording: Boolean = false,
  val recordingSeconds: Int = 0,
  val recordedFilePath: String? = null,
  val recordedDurationSec: Int = 0,
  val errorMessage: String? = null,
)

data class AudioPlaybackState(
  val playingKey: String? = null,
  val isPlaying: Boolean = false,
  val isPreparing: Boolean = false,
  val positionSec: Int = 0,
  val durationSec: Int = 0,
)

class SupportAudioHelper(private val context: Context) {
  private val appContext = context.applicationContext
  private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

  private var recorder: MediaRecorder? = null
  private var recordTimerJob: Job? = null
  private var currentOutputFile: File? = null

  private var player: MediaPlayer? = null
  private var playTickerJob: Job? = null

  private val _recorderState = MutableStateFlow(AudioRecorderState())
  val recorderState: StateFlow<AudioRecorderState> = _recorderState.asStateFlow()

  private val _playbackState = MutableStateFlow(AudioPlaybackState())
  val playbackState: StateFlow<AudioPlaybackState> = _playbackState.asStateFlow()

  fun startRecording() {
    stopPlayback()
    deleteDraftRecording()

    val file = File(appContext.cacheDir, "support_voice_${UUID.randomUUID()}.m4a")
    try {
      @Suppress("DEPRECATION")
      val mr =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) MediaRecorder(appContext)
        else MediaRecorder()

      mr.apply {
        setAudioSource(MediaRecorder.AudioSource.MIC)
        setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
        setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
        setAudioSamplingRate(44_100)
        setAudioEncodingBitRate(64_000)
        setMaxDuration(MAX_RECORDING_SECONDS * 1000)
        setOutputFile(file.absolutePath)
        prepare()
        start()
      }

      recorder = mr
      currentOutputFile = file
      _recorderState.value = AudioRecorderState(isRecording = true, recordingSeconds = 0)

      recordTimerJob?.cancel()
      recordTimerJob =
        scope.launch {
          var elapsed = 0
          while (isActive && _recorderState.value.isRecording) {
            delay(1000L)
            elapsed += 1
            if (elapsed >= MAX_RECORDING_SECONDS) {
              stopRecording()
              break
            }
            _recorderState.value = _recorderState.value.copy(recordingSeconds = elapsed)
          }
        }
    } catch (e: Exception) {
      runCatching { file.delete() }
      cleanupRecorder()
      _recorderState.value =
        AudioRecorderState(
          errorMessage = e.localizedMessage ?: "Unable to start microphone recording."
        )
    }
  }

  fun stopRecording() {
    recordTimerJob?.cancel()
    recordTimerJob = null
    val elapsed = maxOf(1, _recorderState.value.recordingSeconds)
    val file = currentOutputFile

    try {
      recorder?.apply {
        stop()
        reset()
        release()
      }
      recorder = null
      if (file != null && file.exists() && file.length() > 0L) {
        _recorderState.value =
          AudioRecorderState(
            isRecording = false,
            recordingSeconds = elapsed,
            recordedFilePath = file.absolutePath,
            recordedDurationSec = elapsed,
          )
      } else {
        runCatching { file?.delete() }
        currentOutputFile = null
        _recorderState.value = AudioRecorderState(errorMessage = "Recording was too short.")
      }
    } catch (e: Exception) {
      cleanupRecorder()
      runCatching { file?.delete() }
      currentOutputFile = null
      _recorderState.value =
        AudioRecorderState(
          errorMessage = "Hold at least 1 second to record a voice message."
        )
    }
  }

  fun readRecordedBytes(): ByteArray? {
    val path = _recorderState.value.recordedFilePath ?: return null
    val file = File(path)
    return if (file.exists()) runCatching { file.readBytes() }.getOrNull() else null
  }

  fun deleteDraftRecording() {
    if (_playbackState.value.playingKey == DRAFT_PREVIEW_KEY) {
      stopPlayback()
    }
    _recorderState.value.recordedFilePath?.let { path -> runCatching { File(path).delete() } }
    currentOutputFile?.let { file -> runCatching { file.delete() } }
    currentOutputFile = null
    _recorderState.value = AudioRecorderState()
  }

  fun clearError() {
    _recorderState.value = _recorderState.value.copy(errorMessage = null)
  }

  fun toggleDraftPreview() {
    val path = _recorderState.value.recordedFilePath ?: return
    val current = _playbackState.value
    if (current.playingKey == DRAFT_PREVIEW_KEY && current.isPlaying) {
      pausePlayback()
    } else if (current.playingKey == DRAFT_PREVIEW_KEY && !current.isPreparing) {
      resumePlayback()
    } else {
      playSource(
        key = DRAFT_PREVIEW_KEY,
        source = path,
        durationHintSec = _recorderState.value.recordedDurationSec,
      )
    }
  }

  fun toggleRemoteMessagePlayback(messageId: String, url: String, durationHintSec: Int) {
    if (url.isBlank()) return
    val current = _playbackState.value
    if (current.playingKey == messageId && current.isPlaying) {
      pausePlayback()
    } else if (current.playingKey == messageId && !current.isPreparing) {
      resumePlayback()
    } else {
      playSource(key = messageId, source = url, durationHintSec = durationHintSec)
    }
  }

  private fun playSource(key: String, source: String, durationHintSec: Int) {
    stopPlayback()
    _playbackState.value =
      AudioPlaybackState(
        playingKey = key,
        isPlaying = false,
        isPreparing = true,
        positionSec = 0,
        durationSec = maxOf(1, durationHintSec),
      )

    try {
      val mp =
        MediaPlayer().apply {
          setAudioAttributes(
            AudioAttributes.Builder()
              .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
              .setUsage(AudioAttributes.USAGE_MEDIA)
              .build()
          )
          setDataSource(source)
          setOnPreparedListener { readyPlayer ->
            val actualDuration = (readyPlayer.duration / 1000).coerceAtLeast(durationHintSec).coerceAtLeast(1)
            readyPlayer.start()
            _playbackState.value =
              AudioPlaybackState(
                playingKey = key,
                isPlaying = true,
                isPreparing = false,
                positionSec = 0,
                durationSec = actualDuration,
              )
            startPlaybackTicker(key, actualDuration)
          }
          setOnCompletionListener { stopPlayback() }
          setOnErrorListener { _, _, _ ->
            stopPlayback()
            true
          }
          prepareAsync()
        }
      player = mp
    } catch (_: Exception) {
      stopPlayback()
    }
  }

  private fun pausePlayback() {
    runCatching { player?.pause() }
    playTickerJob?.cancel()
    _playbackState.value = _playbackState.value.copy(isPlaying = false)
  }

  private fun resumePlayback() {
    val key = _playbackState.value.playingKey ?: return
    val durationSec = _playbackState.value.durationSec
    runCatching { player?.start() }
    _playbackState.value = _playbackState.value.copy(isPlaying = true)
    startPlaybackTicker(key, durationSec)
  }

  private fun startPlaybackTicker(key: String, durationSec: Int) {
    playTickerJob?.cancel()
    playTickerJob =
      scope.launch {
        while (isActive && _playbackState.value.isPlaying && _playbackState.value.playingKey == key) {
          val posSec = runCatching { (player?.currentPosition ?: 0) / 1000 }.getOrDefault(0)
          _playbackState.value =
            _playbackState.value.copy(positionSec = posSec.coerceIn(0, maxOf(1, durationSec)))
          delay(250L)
        }
      }
  }

  fun stopPlayback() {
    playTickerJob?.cancel()
    playTickerJob = null
    runCatching {
      player?.stop()
      player?.release()
    }
    player = null
    _playbackState.value = AudioPlaybackState()
  }

  private fun cleanupRecorder() {
    recordTimerJob?.cancel()
    recordTimerJob = null
    runCatching {
      recorder?.reset()
      recorder?.release()
    }
    recorder = null
  }

  fun release() {
    cleanupRecorder()
    stopPlayback()
    deleteDraftRecording()
  }

  companion object {
    const val DRAFT_PREVIEW_KEY = "__draft_voice_preview__"
    const val MAX_RECORDING_SECONDS = 120
  }
}
