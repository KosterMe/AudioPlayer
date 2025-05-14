package com.samsung.audioplayer;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import java.util.Random;

public class ComplexWaveVisualizerView extends View {

    private Paint paint;
    private float[] radii;
    private float[] speeds;
    private float[] phases;
    private float phaseShift;
    private int waveCount = 5;
    private Random random;
    private int[] colors;
    private boolean isPlaying = false; // Проверка, играет ли музыка

    public ComplexWaveVisualizerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        paint = new Paint();
        paint.setStyle(Paint.Style.FILL);
        radii = new float[waveCount];
        speeds = new float[waveCount];
        phases = new float[waveCount];
        colors = new int[waveCount];
        random = new Random();
        phaseShift = 0;
        initWaveProperties();
    }

    private void initWaveProperties() {
        for (int i = 0; i < waveCount; i++) {
            // Ограничиваем радиус, чтобы они не занимали весь экран
            radii[i] = random.nextInt(150) + 50;  // Максимальный радиус 150, минимальный 50
            speeds[i] = random.nextFloat() * 0.5f + 0.3f;  // Скорость движения волны
            phases[i] = random.nextFloat() * 100f;  // Начальная фаза
            colors[i] = Color.argb(255, random.nextInt(256), random.nextInt(256), random.nextInt(256));  // Случайные цвета
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int width = getWidth();
        int height = getHeight();
        int centerX = width / 2;
        int centerY = height / 2;

        for (int i = 0; i < waveCount; i++) {
            // Если музыка не играет, просто рисуем последний радиус, не изменяя его
            if (!isPlaying) {
                paint.setColor(colors[i]);
                canvas.drawCircle(centerX, centerY, radii[i], paint);
            } else {
                // Если музыка играет, обновляем радиус волны
                float radius = (float) (radii[i] + Math.sin(phaseShift + i) * 5);  // Уменьшаем амплитуду колебания радиуса
                paint.setColor(colors[i]);
                canvas.drawCircle(centerX, centerY, radius, paint);
                radii[i] = radius;  // Сохраняем новый радиус для последующего обновления
            }
        }

        // Обновляем фазы для анимации
        updateWaves();
        invalidate();  // Повторная отрисовка для плавной анимации
    }

    private void updateWaves() {
        // Плавное изменение фаз
        for (int i = 0; i < waveCount; i++) {
            if (isPlaying) {
                phases[i] += speeds[i];
                speeds[i] += 0.01f;  // Постепенное увеличение скорости
            }
        }
        phaseShift += 0.03f;  // Увеличиваем фазу для уникальных волн
    }

    // Метод для установки состояния воспроизведения
    public void setPlaying(boolean isPlaying) {
        this.isPlaying = isPlaying;  // Обновляем состояние воспроизведения
        invalidate();
    }
}
