package com.hfad.flowers;


import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;


/**
 * Класс фрагмента {@link Fragment} с расписанием.
 * @author Aleks Kitzkalov
 * @version 1.0
 */
public class Schedule extends Fragment {

    private SQLiteDatabase db;
    private Cursor cursor;

    public Schedule() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_schedule, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setupListView();
    }

    /**
     * Метод свзывания данных из таблицы SCHEDULE базы данных с элементом ListView фрагмента.
     */
    private void setupListView() {
        SQLiteOpenHelper flowersDatabaseHelper = new FlowersDatabaseHelper(this.getContext());

        // Получение элемента ListView
        ListView listView = (ListView)getView().findViewById(R.id.schedule_list_item);

        try {
            // Получение базы данных
            db = flowersDatabaseHelper.getReadableDatabase();

            // Получение курсора с записями
            cursor = db.query("SCHEDULE", new String[]{"_id", "NAME", "TYPE", "DATE"},
                    null, null, null, null, null);

            // Создание Адаптера Курсора
            SimpleCursorAdapter listAdapter = new SimpleCursorAdapter(this.getContext(),
                    R.layout.schedule_item_view,                                            // Макет элемента списка
                    cursor,
                    new String[]{"DATE", "NAME", "TYPE"},                                   // Значения из курсора для связывания с элементами макета
                    new int[]{R.id.date, R.id.name, R.id.event},                            // Элементы макета
                    0);
            listView.setAdapter(listAdapter);                                               // Адаптер связывается с ListView
        } catch (SQLiteException e) {
            // Вывести сообщение об ошибке, если база данных недоступна
            Toast.makeText(this.getContext(), "Database unavailable", Toast.LENGTH_SHORT).show();
        }
    }
}
