package com.hfad.flowers;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.AsyncTask;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

/**
 * Класс главной Активности приложения.
 * @autor Aleks Kitzkalov
 * @version 1.0
 */
public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, AllFlowers.Listener {

    /**
     * Метод создания Активности.
     * @param savedInstanceState - сохраненные данные Активности
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Установка панели инструментов
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Запуск обновления дат в SCHEDULE
        new UpdateSchedule().execute();

        // Добавление кнопки вызова панели инструментов
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this,
                drawer,
                toolbar,
                R.string.nav_open_drawer,
                R.string.nav_close_drawer);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        // Регистрация Активности в качестве слушателя NavigationView
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        // Заполнение Активности фрагментом MainScreen
        Fragment fragment = new MainScreen();
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.add(R.id.content_frame, fragment);
        ft.commit();
    }

    /**
     * Метод обработки выбора команды на навигационной панели.
     * @param item - объект на котором был сделан щелчок
     * @return Флаг, указывающий, нужно ли выделить команду на навигационной панели
     */
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();      // ID выбранной команды
        Fragment fragment = null;
        Intent intent = null;

        // Добавление фрагмента к активности
        // или запуск новой активности в зависимости от id
        switch (id) {
            case R.id.nav_my_flowers:
                fragment = new AllFlowers();
                ((AllFlowers) fragment).i = true;
                break;
            case R.id.nav_all_flowers:
                fragment = new AllFlowers();
                ((AllFlowers) fragment).i = false;
                break;
            case R.id.nav_schedule:
                fragment = new Schedule();
                break;
            case R.id.nav_settings:
                intent = new Intent(this, Settings.class);
                break;
            default:
                fragment = new MainScreen();
        }

        if (fragment != null) {
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.content_frame, fragment);
            ft.commit();
        } else {
            startActivity(intent);
        }

        // Закрытие панели после сделанного выбора
        DrawerLayout drawer = (DrawerLayout)findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    /**
     * Метод закрытия навигационной панели при нажатии кнопки Назад.
     */
    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    /**
     * Метод открывает Активность Flowers при выборе цветка во фрагменте AllFlowers.
     * @param name - имя цветка
     */
    @Override
    public void itemClicked(String name) {
        Intent intent = new Intent(this, Flower.class);
        intent.putExtra(Flower.EXTRA_NAME, name);
        startActivity(intent);
    }

    /**
     * Класс для выполнение задач в фоновом режиме.
     * @author Aleks Kitzkalov
     * @version 1.0
     */
    private class UpdateSchedule extends AsyncTask<Void, Void, Void> {
        /**
         * Основной метод фонового процесса
         * Метод актуализации поля DATE таблицы SCHEDULE.
         */
        @Override
        protected Void doInBackground(Void... voids) {
            // Получаем все записи из таблицы SCHEDULE
            SQLiteOpenHelper sqLiteOpenHelper = new FlowersDatabaseHelper(MainActivity.this);
            SQLiteDatabase sqLiteDatabase = sqLiteOpenHelper.getReadableDatabase();
            Cursor cursorForUpdate = sqLiteDatabase.query("SCHEDULE", new String[]{"NAME", "TYPE", "DATE", "FREQUENCY"},
                    null, null, null, null, null);

            // Получаем текущую дату
            Calendar now = GregorianCalendar.getInstance();

            // Пока в курсоре есть записи
            while (cursorForUpdate.moveToNext()) {

                // Получаем частоту полива/обрызгивания
                int frequency = cursorForUpdate.getInt(3) / 24;

                // Получаем дату из текущей записи курсора
                Calendar calendar = GregorianCalendar.getInstance();
                DateFormat format = new SimpleDateFormat("dd MMMM yyyy", new Locale("ru"));
                try {
                    Date date = format.parse(cursorForUpdate.getString(2));
                    calendar.setTime(date);
                } catch (ParseException e) {
                    e.printStackTrace();
                }

                // Результат сравнения дней, true - если текущая дата больше даты в курсоре, иначе - false
                boolean sameDay = calendar.get(Calendar.YEAR) < now.get(Calendar.YEAR) ||
                        (calendar.get(Calendar.YEAR) == now.get(Calendar.YEAR) &&
                                calendar.get(Calendar.DAY_OF_YEAR) < now.get(Calendar.DAY_OF_YEAR));

                if (sameDay) {
                    long difMils = now.getTimeInMillis() - calendar.getTimeInMillis();          // Разница дат в мс
                    double difDay = Math.floor((double) difMils / (1000 * 60 * 60 * 24));       // Разница дат в днях
                    int plus = ((int) Math.ceil(difDay / frequency)) * frequency;               // Число дней, которые необходимо прибавить дате в курсоре
                    calendar.add(Calendar.DATE, plus);                                          // Изменить дату ближайшего полива/обрызгивания на актуальную
                }

                //Новая дата ближайшего полива/обрызгивания в формате строки
                String resultDate = new SimpleDateFormat("dd MMMM yyyy", new Locale("ru")).format(calendar.getTime());

                // Обновляем дату ближайшего полива/обрызгивания для записи
                // Параметры: БД, Имя, Тип, Новая дата
                FlowersDatabaseHelper.updateSchedule(sqLiteDatabase, cursorForUpdate.getString(0),
                        cursorForUpdate.getString(1), resultDate);
            }

            // Закрываем базу данных и курсор
            sqLiteOpenHelper.close();
            sqLiteDatabase.close();
            cursorForUpdate.close();
            return null;
        }
    }
}
