package com.application.workouttimerapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.widget.EditText;
import android.widget.Toast;

import com.application.workouttimerapp.databinding.ActivityMainBinding;

import java.io.IOException;
import java.util.stream.IntStream;

public class MainActivity extends AppCompatActivity {

    ActivityMainBinding binding;
    int totalTime;
    CountDownTimer progressTimer;
    CountDownTimer setTimer;
    CountDownTimer restTimer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.startButton.setOnClickListener(v -> {
            // validate if setDuration, restDuration, and numberOfSets is assigned or not
            if (binding.setDuration.getText().toString().isEmpty()) {
                binding.setDuration.setError("Please enter set duration");
                return;
            } else if (binding.restDuration.getText().toString().isEmpty()) {
                binding.restDuration.setError("Please enter rest duration");
                return;
            } else if (binding.sets.getText().toString().isEmpty()) {
                binding.sets.setError("Please enter number of sets");
                return;
            } else {
                totalTime = getTotalTime(binding.setDuration, binding.restDuration, binding.sets);
                binding.progressBar.setMax(totalTime);

                progressTimer = new CountDownTimer(totalTime * 1000L, 1000) {
                    @Override
                    public void onTick(long l) {
                        binding.progressBar.setProgress((int) (totalTime - l / 1000), true);
                    }

                    @Override
                    public void onFinish() {
                        // Send notification
                        Context context = getBaseContext();

                        // Create a MediaPlayer object and set the audio file to play
                        MediaPlayer mediaPlayer = new MediaPlayer();
                        try {
                            mediaPlayer.setDataSource(context, Uri.parse("android.resource://" + context.getPackageName() + "/" + R.raw.notifacation));
                            mediaPlayer.prepare();
                            mediaPlayer.start();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        // Create a notification channel
                        String channelId = "workout_channel";
                        String channelName = "Workout Notifications";
                        NotificationChannel channel = new NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_DEFAULT);
                        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                        notificationManager.createNotificationChannel(channel);

                        // Create the notification
                        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, channelId)
                                .setSmallIcon(R.drawable.icon)
                                .setContentTitle("Workout Complete")
                                .setContentText("Congratulations! Your workout is complete.")
                                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

                        // Create the intent and PendingIntent
                        Intent intent = new Intent(context, MainActivity.class);
                        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
                        builder.setContentIntent(pendingIntent);

                        // Show the notification
                        int notificationId = (int) System.currentTimeMillis(); // Use a unique id for the notification
                        notificationManager.notify(notificationId, builder.build());
                    }
                };
                progressTimer.start();
                startTimer(0, Integer.parseInt(binding.sets.getText().toString()));
            }
        });

        binding.stopButton.setOnClickListener(v -> {
            binding.timeRemaining.setText("Workout stopped");
            if (progressTimer != null)
                progressTimer.cancel();
            if (setTimer != null)
                setTimer.cancel();
            if (restTimer != null)
                restTimer.cancel();
        });

    }

    void startTimer(int iteration, int totalIterations) {
        setTimer = new CountDownTimer(Integer.parseInt(binding.setDuration.getText().toString()) * 1000L, 1000) {
            @Override
            public void onTick(long l) {
                binding.timeRemaining.setText("Set Time Remaining: " + getTimeFromMillis(l));
            }

            @Override
            public void onFinish() {
                if (iteration != totalIterations - 1) {
                    // Send notification
                    Toast.makeText(MainActivity.this, "Set " + (iteration + 1) + " finished", Toast.LENGTH_SHORT).show();

                    // Start rest timer
                    restTimer = new CountDownTimer(Integer.parseInt(binding.restDuration.getText().toString()) * 1000L, 1000) {
                        @Override
                        public void onTick(long l) {
                            binding.timeRemaining.setText("Rest Time Remaining: " + getTimeFromMillis(l));
                        }

                        @Override
                        public void onFinish() {
                            // Send notification
                            Toast.makeText(MainActivity.this, "Rest finished", Toast.LENGTH_SHORT).show();
                            startTimer(iteration + 1, totalIterations);
                        }
                    };
                    restTimer.start();
                } else {
                    Toast.makeText(MainActivity.this, "Workout finished", Toast.LENGTH_SHORT).show();
                    binding.timeRemaining.setText("Workout finished");
                }
            }
        };
        setTimer.start();
    }


    int getIntFromEditText(EditText editText) {
        return Integer.parseInt(editText.getText().toString());
    }

    int getTotalTime(EditText setDuration, EditText restDuration, EditText reps) {
        return (getIntFromEditText(setDuration) * getIntFromEditText(reps)) + (getIntFromEditText(restDuration) * (getIntFromEditText(reps) - 1));
    }

    String getTimeFromMillis(long millis) {
        int seconds = (int) (millis / 1000);
        int minutes = seconds / 60;
        seconds = (seconds % 60) + 1;

        return String.format("%02d", minutes) + ":" + String.format("%02d", seconds);
    }

}