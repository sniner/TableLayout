# TableLayout

## Motivation

Quite an old piece of Java code: I wrote `TableLayout` layout manager back in 2002, because
most of the time I found `GridLayout` and `GridBagLayout` far too complex. At that time I used
LaTeX for all documentation stuff and therefore it was quite natural to me to take the
table definition syntax of LaTeX. Despite being quite old I still use TableLayout regularily
and there is even a Scala class `TablePanel` included. As `TableLayout` is useful for me,
maybe it is useful for you too.

## Usage

As the name `TableLayout` implies, it's purpose is a table-like arrangement of Java
Swing elements. For example: many dialog boxes use a 2-column layout, the first columns has
labels and the second column elements for data entry.

With `TableLayout` this is quite easy to accomplish:

```
    ...
    panel.setLayout(new TableLayout("ll"))
    ...
    JLabel label1 = new Label("This data")
    JTextField field1 = new JTextField()
    JLabel label2 = new Label("And that data")
    JTextField field2 = new JTextField()
    panel.add(label1)
    panel.add(field1)
    panel.add(label2)
    panel.add(field2)
    ...
```

As you can see, the table layout is supplied as a string of single characters:

* l: a left justified column of fixed size
* r: right jusitification
* c: all elements in this column centered
* f: fill the remaining space

If you are accustomed to LaTeX tables, it should look familiar to you. The width of
the justified columns are derived from the elements in the column itself: the column
width is the width of the biggest element in that column. Fill columns grow and shrink
as required, all elements in a fill column are expanded to the column width.

There are optional second and third parameters to TableLayout: row padding and column
padding.

That's all.

## Documentation

Sorry, there is no further documentation except this readme file.
