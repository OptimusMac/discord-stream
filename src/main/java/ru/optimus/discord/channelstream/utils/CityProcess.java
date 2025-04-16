package ru.optimus.discord.channelstream.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Random;

@Component
@Slf4j
public class CityProcess {

    private static final Random random = new Random();
    private static final String[] prevWords = new String['Я' - 'А' + 1];
    private static String[][] citiesByFirstLetter = {
            /* А */ {"Абакан", "Азов", "Александров", "Альметьевск", "Анапа", "Архангельск", "Астрахань", "Армавир", "Артём", "Ачинск"},
            /* Б */ {"Балаково", "Балашиха", "Барнаул", "Батайск", "Белгород", "Бердск", "Бийск", "Благовещенск", "Братск", "Брянск"},
            /* В */ {"Видное", "Владивосток", "Владикавказ", "Владимир", "Волгоград", "Волгодонск", "Волжский", "Вологда", "Воркута", "Воронеж"},
            /* Г */ {"Гатчина", "Геленджик", "Глазов", "Горно-Алтайск", "Грозный", "Губкинский", "Гусь-Хрустальный"},
            /* Д */ {"Дербент", "Дзержинск", "Димитровград", "Дмитров", "Долгопрудный", "Домодедово", "Донецк", "Дубна"},
            /* Е */ {"Евпатория", "Егорьевск", "Ейск", "Екатеринбург", "Елабуга", "Елец", "Ессентуки"},
            /* Ж */ {"Железногорск", "Жигулёвск", "Жуковский"},
            /* З */ {"Заречный", "Зеленогорск", "Зеленодольск", "Златоуст"},
            /* И */ {"Иваново", "Ивантеевка", "Ижевск", "Иркутск", "Искитим", "Ишим", "Ишимбай"},
            /* Й */ {"Йошкар-Ола"},
            /* К */ {"Казань", "Калининград", "Калуга", "Каменск-Уральский", "Камышин", "Канск", "Кемерово", "Керчь", "Киров", "Кисловодск", "Ковров", "Коломна", "Комсомольск-на-Амуре", "Копейск", "Королёв", "Кострома", "Красногорск", "Краснодар", "Красноярск", "Курган", "Курск", "Кызыл"},
            /* Л */ {"Лениногорск", "Ленинск-Кузнецкий", "Липецк", "Лобня", "Люберцы"},
            /* М */ {"Магадан", "Магнитогорск", "Майкоп", "Махачкала", "Миасс", "Москва", "Мурманск", "Муром", "Мытищи"},
            /* Н */ {"Набережные Челны", "Назарово", "Назрань", "Нальчик", "Наро-Фоминск", "Нефтекамск", "Нефтеюганск", "Нижневартовск", "Нижнекамск", "Нижний Новгород", "Новокузнецк", "Новороссийск", "Новосибирск", "Ногинск", "Норильск", "Ноябрьск"},
            /* О */ {"Обнинск", "Одинцово", "Октябрьский", "Омск", "Орёл", "Оренбург", "Орехово-Зуево", "Орск"},
            /* П */ {"Павлово", "Павловский Посад", "Пенза", "Пермь", "Петрозаводск", "Петропавловск-Камчатский", "Подольск", "Прокопьевск", "Псков", "Пушкино", "Пятигорск"},
            /* Р */ {"Раменское", "Ревда", "Реутов", "Ржев", "Рославль", "Россошь", "Ростов-на-Дону", "Рубцовск", "Рыбинск", "Рязань"},
            /* С */ {"Салават", "Салехард", "Самара", "Санкт-Петербург", "Саранск", "Сарапул", "Саратов", "Северодвинск", "Северск", "Сергиев Посад", "Серов", "Серпухов", "Симферополь", "Смоленск", "Соликамск", "Сочи", "Ставрополь", "Старый Оскол", "Стерлитамак", "Ступино", "Сургут", "Сызрань", "Сыктывкар"},
            /* Т */ {"Тамбов", "Тверь", "Тимашёвск", "Тихвин", "Тихорецк", "Тобольск", "Тольятти", "Томск", "Троицк", "Туапсе", "Тула", "Тюмень"},
            /* У */ {"Улан-Удэ", "Ульяновск", "Уссурийск", "Уфа", "Ухта"},
            /* Ф */ {"Феодосия", "Фрязино"},
            /* Х */ {"Хабаровск", "Ханты-Мансийск", "Химки", "Холмск", "Чебоксары", "Челябинск", "Череповец", "Черкесск", "Черногорск", "Чехов", "Чита", "Чусовой"},
            /* Ц */ {"Цивильск"},
            /* Ч */ {"Чебоксары", "Челябинск", "Череповец", "Черкесск", "Черногорск", "Чехов", "Чита", "Чусовой"},
            /* Ш */ {"Шадринск", "Шали", "Шахты", "Шуя"},
            /* Щ */ {"Щёлково", "Щёкино", "Щербинка"},
            /* Э */ {"Электросталь", "Элиста", "Энгельс"},
            /* Ю */ {"Южно-Сахалинск", "Юрга"},
            /* Я */ {"Якутск", "Ялта", "Ярославль"}
    };


    public synchronized static String findWord(char c) {
        if (c < 'А' || c > 'Я') {

            return null;
        }



        int index = c - 'А';

        if (citiesByFirstLetter == null || index >= citiesByFirstLetter.length
                || citiesByFirstLetter[index] == null
                || citiesByFirstLetter[index].length == 0) {
            return null;
        }
        String[] cities = citiesByFirstLetter[index];
        String selectedCity;
        int attempts = 0;
        final int maxAttempts = 5;

        do {
            selectedCity = cities[random.nextInt(cities.length)];
            attempts++;
        } while (attempts < maxAttempts && selectedCity.equals(prevWords[index]));

        if (selectedCity.equals(prevWords[index])) {
            selectedCity = findWord(c);
        }

        prevWords[index] = selectedCity;
        return selectedCity;
    }


    public static boolean validateCity(String city){

        for (int i = 0; i < citiesByFirstLetter.length; i++) {
            String[] wordsArray = citiesByFirstLetter[i];
            for (int i1 = 0; i1 < wordsArray.length; i1++) {
                String word = wordsArray[i1];
                if(word.equalsIgnoreCase(city)){
                    return true;
                }
            }
        }
        return false;
    }
}
