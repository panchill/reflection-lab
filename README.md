# Лабораторная работа № 2. Java Reflection — генератор объектов

Генератор случайных значений по `Class<?>`. Работает с примитивами, строками, enum, массивами, `List`, `Map`, пользовательскими классами, интерфейсами и абстрактными классами. Пользовательские типы исследуются и создаются через `java.lang.reflect`. Отдельных веток под имена конкретных классов нет.

```java
Generator g = new Generator();

Integer number   = g.generateValueOfType(Integer.class);
String text      = g.generateValueOfType(String.class);
Rectangle rect   = g.generateValueOfType(Rectangle.class);
Shape shape      = g.generateValueOfType(Shape.class);
int[] numbers    = g.generateValueOfType(int[].class);
List<?> items    = g.generateValueOfType(List.class);
Map<?, ?> map    = g.generateValueOfType(Map.class);
```

## Запуск

```bash
./gradlew test        # Linux / macOS
gradlew.bat test      # Windows
```

Используются Java 21 (Gradle toolchain), Gradle 8.14 и JUnit 5. Тесты автоматически запускаются в GitHub Actions при `push` и `pull_request` (`.github/workflows/ci.yml`).

## Архитектура

Используются паттерны **Strategy** и **Chain of Responsibility**. Каждая категория типов обрабатывается отдельным классом, который реализует интерфейс `TypeGenerator`:

```java
public interface TypeGenerator {
    boolean supports(Class<?> type);                       
    Object generate(Class<?> type, GenerationContext ctx);
}
```

`Generator` перебирает стратегии по порядку и отдаёт тип первой, которая его поддерживает. Он сам ничего не знает о конкретных типах. Чтобы добавить новую категорию, достаточно написать новый `TypeGenerator` и добавить его в список. Сам `Generator` при этом не меняется.

```
org.example.generator
├── Generator              фасад: список стратегий, лимит глубины
├── GenerationContext      Random, текущая глубина, путь создаваемых типов, рекурсивный вызов
├── GenerationException    единое исключение генерации
├── Generatable            аннотация
└── types
    ├── TypeGenerator
    ├── IntegerGenerator, ByteGenerator, ShortGenerator, LongGenerator,
    │   FloatGenerator, DoubleGenerator, BooleanGenerator, CharacterGenerator
    ├── StringGenerator
    ├── EnumGenerator
    ├── ArrayGenerator
    ├── ListGenerator
    ├── MapGenerator
    ├── AbstractTypeGenerator   интерфейсы и абстрактные классы
    └── ObjectGenerator         конкретные классы через конструктор (последний в цепочке)
```

**Порядок стратегий важен.** `ListGenerator` и `MapGenerator` стоят раньше `AbstractTypeGenerator`, потому что `List` и `Map` — интерфейсы. `ObjectGenerator` стоит последним.

**Рекурсия.** Стратегии не создают новый `Generator`. Вложенные значения (элементы массивов и коллекций, аргументы конструкторов, реализации интерфейсов) они запрашивают через `ctx.generate(type)`. Этот вызов увеличивает глубину и запоминает путь.

## Поддерживаемые типы и параметры генерации

| Категория | Как генерируется |
|---|---|
| `int`/`Integer`, `byte`/`Byte`, `short`/`Short`, `long`/`Long` | случайное целое от 0 до 99 |
| `float`/`Float`, `double`/`Double` | случайное число в [0, 100) |
| `boolean`/`Boolean` | `true` или `false` |
| `char`/`Character` | строчная латинская буква `a`–`z` |
| `String` | `"str"` + случайное число от 0 до 999 |
| enum | случайная константа из `getEnumConstants()` |
| Массивы | длина 1–5, элементы генерируются рекурсивно по `getComponentType()`. Поддерживаются массивы любых поддерживаемых типов, включая многомерные |
| `List` | `ArrayList` из 1–5 элементов |
| `Map` | `HashMap` из 1–5 пар (ключи могут совпасть, поэтому пар может оказаться меньше) |
| Конкретные классы | случайный объявленный конструктор (включая `private`) с рекурсивно сгенерированными аргументами |
| Интерфейсы и абстрактные классы | случайная реализация из `@Generatable` |

### Generic-типы `List` и `Map`

Из-за type erasure по `List.class` нельзя узнать тип элементов. Поэтому:

- **Прямой запрос** `generateValueOfType(List.class)` даёт `List<Integer>`, а `generateValueOfType(Map.class)` даёт `Map<String, Integer>`.
- **Параметр конструктора** вида `List<X>` или `Map<K, V>`: тип элементов читается из сигнатуры через `Constructor.getGenericParameterTypes()` и `ParameterizedType`. Например, для `Cart(List<Product> items)` генерируется список `Product`.

## Аннотация `@Generatable`

```java
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Generatable {
    Class<?>[] implementations() default {};
}
```

**Семантика:** аннотация задаёт, какими реализациями можно подменить абстрактный тип.

- **Интерфейс или абстрактный класс** можно сгенерировать **только** при наличии `@Generatable`. Генератор выбирает случайный класс из `implementations` и создаёт его. Реализация может сама быть абстрактной со своей `@Generatable`, тогда выбор продолжается рекурсивно.
  ```java
  @Generatable(implementations = {Rectangle.class, Triangle.class})
  public interface Shape { ... }
  ```
- **Конкретные классы** аннотация не требует, они создаются через конструктор. Если повесить её на конкретный класс, она ни на что не влияет.
- **Аннотация не наследуется** (нет `@Inherited`). Каждый абстрактный тип объявляет свои реализации сам: абстрактный подкласс аннотированного класса без собственной `@Generatable` сгенерировать нельзя.
- **Проверки.** Бросается `GenerationException`, если:
    - аннотации нет;
    - список `implementations` пуст;
    - указанный класс не является подтипом аннотированного типа (`isAssignableFrom`).

Зачем аннотация нужна: Java не умеет перечислять реализации интерфейса во время выполнения, а генератору надо откуда-то их взять. `@Generatable` — единственный источник этой информации, без неё генерация по `Shape.class` невозможна.

## Рекурсия и циклические ссылки

**Циклические ссылки** — это классы, которые содержат сами себя, например `BinaryTreeNode(Integer data, BinaryTreeNode left, BinaryTreeNode right)`. `GenerationContext` хранит путь — цепочку типов, которые сейчас создаются. Если тип параметра конструктора уже есть в пути, ссылка считается циклической:

- с вероятностью 50% вместо неё подставляется `null`;
- начиная с глубины 4, всегда подставляется `null`.

Так получаются случайные деревья и цепочки конечного размера. Здесь `null` — значение поля (лист дерева), а не признак ошибки.

**Лимит глубины.** Если вложенность превысила 10 уровней (например, 12-мерный массив), бросается `GenerationException`.

## Ошибки

Любая неудача заканчивается `GenerationException` (unchecked). В сообщении указан тип, который не удалось сгенерировать, и причина. Если упал сам конструктор, исходное исключение сохраняется в `getCause()`.

## Ограничения

- Конструктор выбирается случайно. Если у класса несколько конструкторов и один из них требует неподдерживаемый тип, генерация может то проходить, то падать.
- `List` всегда `ArrayList`, `Map` всегда `HashMap`. Другие коллекции (`Set`, `Queue`, `LinkedList`, `TreeMap`) не поддерживаются.
- Generic-информация читается только из параметров конструктора и только для прямых аргументов-классов. Wildcard (`List<? extends Product>`) и вложенные generic (`List<List<Product>>`) обрабатываются как тип по умолчанию.
- Поля не заполняются напрямую, объект создаётся только через конструктор.
- Нестатические внутренние классы не поддерживаются полноценно: их конструктор неявно требует экземпляр внешнего класса.
- Приватные конструкторы классов JDK недоступны из-за системы модулей (Java 9+).
- Сгенерированные значения не учитывают предметные ограничения (например, неравенство треугольника).
- `void`/`Void` не генерируются.

## Тесты

`src/test/java/org/example/generator/GeneratorTest.java` проверяет:

- все примитивы и обёртки, `String`, enum, одномерные и многомерные массивы, `List`, `Map`;
- все классы из пакета `classes`: `Rectangle`, `Triangle`, `Product`, `Example`, `BinaryTreeNode`, `Cart` с элементами типа `Product`, а также генерацию по `Shape.class`;
- собственные классы: несколько параметров разных типов, `private`-конструктор, самоссылающийся класс;
- семантику `@Generatable` для интерфейсов и абстрактных классов, включая все случаи ошибок;
- поведение при неудачной генерации.

Тесты проверяют **свойства** результата (тип, диапазон, длину, непустоту), а не конкретные случайные значения. Для случайных свойств используется `@RepeatedTest`.