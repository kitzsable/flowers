package com.hfad.flowers;


import android.content.Context;
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
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;


/**
 * Класс фрагмента {@link Fragment} со списком цветов.
 * @author Aleks Kitzkalov
 * @version 1.0
 */
public class AllFlowers extends Fragment {

    // Значение выбранного списка (true - мои цветы, false - все цветы)
    public boolean i;

    /**
     * Интерфейс слушателя.
     */
    static interface Listener {
        void itemClicked(String name);
    };

    private Listener listener;      // Переменная для связывания слушателя и активности

    private SQLiteDatabase db;      // Переменная для хранения базы данных
    private Cursor cursor;          // Переменная для хранения курсора базы данных

    public AllFlowers() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        // Связывание слушателя с Активностью
        this.listener = (Listener)context;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_all_flowers, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setupListView();
    }

    @Override
    public void onStart() {
        super.onStart();
        // Обновление данных в ListView
        if(i) {
            Cursor newCursor = db.query("FLOWERS", new String[]{"_id", "NAME", "IMAGE_RESOURCE_ID"},
                    "IN_MY = ?", new String[]{Integer.toString(1)}, null, null, null);
            ListView listView = (ListView)getView().findViewById(R.id.list_item);
            SimpleCursorAdapter adapter = (SimpleCursorAdapter)listView.getAdapter();
            adapter.changeCursor(newCursor);
            cursor = newCursor;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // Закрытие курсора и базы данных
        cursor.close();
        db.close();
    }

    /**
     * Метод свзывания данных из таблицы FLOWERS базы данных с элементом ListView фрагмента.
     */
    private void setupListView() {
        SQLiteOpenHelper flowersDatabaseHelper = new FlowersDatabaseHelper(this.getContext());

        // Получение элемента ListView
        ListView listView = (ListView)getView().findViewById(R.id.list_item);

        // Создание Адаптера курсора в зависимости от выбранного списка
        try {
            if (i) {

                db = flowersDatabaseHelper.getReadableDatabase();

                // Получение курсора с записями, в которых IN_MY = 1
                cursor = db.query("FLOWERS", new String[]{"_id", "NAME", "IMAGE_RESOURCE_ID"},
                        "IN_MY = ?", new String[]{Integer.toString(1)}, null, null, null);

                // Создание Адаптера Курсора
                SimpleCursorAdapter listAdapter = new SimpleCursorAdapter(this.getContext(),
                        R.layout.item_view,                                                     // Макет элемента списка
                        cursor,
                        new String[]{"NAME", "IMAGE_RESOURCE_ID"},                              // Значения из курсора для связывания с элементами макета
                        new int[]{R.id.textViewName, R.id.imageViewIcon},                       // Элементы макета
                        0);
                listView.setAdapter(listAdapter);                                               // Адаптер связывается с ListView

            } else {

                db = flowersDatabaseHelper.getReadableDatabase();

                // Получение курсора со всеми записями
                cursor = db.query("FLOWERS", new String[]{"_id", "NAME", "IMAGE_RESOURCE_ID"},
                        null, null, null, null, null);

                // Создание Адаптера Курсора
                SimpleCursorAdapter listAdapter = new SimpleCursorAdapter(this.getContext(),
                        R.layout.item_view,                                                     // Макет элемента списка
                        cursor,
                        new String[]{"NAME", "IMAGE_RESOURCE_ID"},                              // Значения из курсора для связывания с элементами макета
                        new int[]{R.id.textViewName, R.id.imageViewIcon},                       // Элементы макета
                        0);
                listView.setAdapter(listAdapter);                                               // Адаптер связывается с ListView

            }

        } catch (SQLiteException e) {
            // Вывести сообщение об ошибке, если база данных недоступна
            Toast.makeText(this.getContext(), "Database unavailable", Toast.LENGTH_SHORT).show();
        }

        // Создание слушателя щелков на списке
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> listView, View itemView, int position, long id) {
                // Получение названия цветка у элемента по которомы сделан щелчок
                String name = ((TextView)itemView.findViewById(R.id.textViewName)).getText().toString();
                // Вызов метода Активности для обработки щелчка
                if (listener != null) listener.itemClicked(name);
            }
        });
    }

}
