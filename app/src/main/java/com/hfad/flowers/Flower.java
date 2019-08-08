package com.hfad.flowers;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.CalendarContract;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Calendar;

/**
 * Класс Активности, содаржащей данные цветка.
 * @author Aleks Kitzkalov
 * @version 1.0
 */
public class Flower extends AppCompatActivity {


    private SQLiteDatabase db;      // Переменная для хранения базы данных
    private Cursor cursor;          //  Переменная для хранения курсора базы данных

    public static final String EXTRA_NAME = "name";     // Имя переменно с дополнительной информацией из Интента

    // Поля базы данных
    private String name;
    private String description;
    private int resourcePhotoID;
    private int wateringFrequency;
    private int sprinklingFrequency;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_flower);

        // Установка панели инструментов
        Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Получаем запись из базы данных и сохраняем значение полей в переменных класса
        SQLiteOpenHelper sqLiteOpenHelper = new FlowersDatabaseHelper(this);
        try {
            db = sqLiteOpenHelper.getWritableDatabase();
            cursor = db.query("FLOWERS", new String[]{"NAME", "IMAGE_RESOURCE_ID", "DESCRIPTION", "WATERING_FR", "SPRINKLING_FR", "IN_MY"},
                    "NAME = ?", new String[]{getIntent().getExtras().get((EXTRA_NAME)).toString()}, null, null, null);
            if (cursor.moveToFirst()) {
                name = cursor.getString(0);
                resourcePhotoID = cursor.getInt(1);
                description = cursor.getString(2);
                wateringFrequency = cursor.getInt(3);
                sprinklingFrequency = cursor.getInt(4);
            }
        } catch (SQLiteException e) {
            Toast.makeText(this, "Database unavailable", Toast.LENGTH_SHORT).show();
        }

        // Получаем View-элементы
        TextView nameView = (TextView)findViewById(R.id.name);
        ImageView photoView = (ImageView)findViewById(R.id.photo);
        TextView descriptionView = (TextView)findViewById(R.id.description);

        // Записываем значения во View-элементы
        nameView.setText(name);
        photoView.setContentDescription(name);
        photoView.setImageResource(resourcePhotoID);
        descriptionView.setText(description);
    }

    /**
     * Метод срабатывает при нажатии кнопки "Добавить".
     * @param view - Компонент графического интерфейса, инициировавший вызов метода
     */
    public void addFlower(View view) {
        // Обновить значения столбца IN_MY, если нужно.
        if (cursor.getInt(5) == 0) FlowersDatabaseHelper.updateIN_MY(db, name, 1);

        // Добавление полива в календарь и таблицу SCHEDULE.
        addEvent("полив", name, wateringFrequency);
        FlowersDatabaseHelper.insertSchedule(db, name, "Полив", getStartDate(), wateringFrequency);


        // Добавление обрызгивания в календарь и таблицу SCHEDULE, если это требуется.
        if (sprinklingFrequency != 0) {
            addEvent("обрызгивание", name, sprinklingFrequency);
            FlowersDatabaseHelper.insertSchedule(db, name, "Обрызгивание", getStartDate(), sprinklingFrequency);
        }
    }

    /**
     * Метод срабатывает при нажатии кнопки "Удалить".
     * @param view - Компонент графического интерфейса, инициировавший вызов метода
     */
    public void dropFlower(View view) {
        // Обновить значения столбца IN_MY, если нужно.
        if (cursor.getInt(5) == 1) FlowersDatabaseHelper.updateIN_MY(db, name, 0);

        // Удалить события из таблицы SCHEDULE.
        FlowersDatabaseHelper.dropFromSchedule(db, name);
    }

    /**
     * Метод добавления события в календарь.
     * @param type - Тип события
     * @param name - Имя цветка
     * @param frequency - Частота повторения события
     */
    private void addEvent(String type, String name, int frequency) {
        // Получить частоту полива/обрызгивания в днях
        // и остановить выполнение метода если она равна 0.
        int dayFr = frequency/24;
        if (dayFr == 0) return;

        // Получить описание события в зависимости от его типа
        String desc = "";
        if (type == "полив") {
            desc = "Пора поливать цветочек.";
        } else if (type == "обрызгивание") {
            desc = "Пора обрызгивать цветочек.";
        }

        // Получить дату начала и окончания события
        Calendar beginTime = getStartDate();
        Calendar endTime = getFinishDate(beginTime);

        // Создание и запуск намерения для открытия календаря и создания события
        Intent intent = new Intent(Intent.ACTION_INSERT)
                .setData(CalendarContract.Events.CONTENT_URI)
                .putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, beginTime.getTimeInMillis())
                .putExtra(CalendarContract.EXTRA_EVENT_END_TIME, endTime.getTimeInMillis())
                .putExtra(CalendarContract.Events.TITLE, name)
                .putExtra(CalendarContract.Events.DESCRIPTION, desc)
                .putExtra(CalendarContract.Events.RRULE, "FREQ=DAILY;INTERVAL=" + Integer.toString(dayFr))
                .putExtra(CalendarContract.Events.AVAILABILITY, CalendarContract.Events.AVAILABILITY_BUSY);
        startActivity(intent);
    }

    /**
     * Метод возвращает дату начала события
     * @return Дата начала события
     */
    private Calendar getStartDate() {
        Calendar c = Calendar.getInstance();
        c.add(Calendar.DATE, 1);
        c.set(Calendar.HOUR_OF_DAY, 6);
        c.set(Calendar.MINUTE, 30);
        c.set(Calendar.SECOND, 00);
        return c;
    }

    /**
     * Метод возвращает дату окончания события
     * @return Дата окончания события
     */
    private Calendar getFinishDate(Calendar start) {
        start.add(Calendar.MINUTE, 15);
        return start;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        // Закрытие базы данных и курсора
        cursor.close();
        db.close();
    }
}
