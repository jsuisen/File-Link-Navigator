# File-Link-Navigator
Plugin for Intellij IDE, support File Link Navigator


In Java code comments, you can link to any line in any text file (e.g., a Markdown file) within the project by using a special format.

It supports hovering, Ctrl+Click, or keyboard shortcuts to jump to the specified line in the target file.

Custom configurations are available in the settings page.

在 Java 代码注释中，通过特殊格式即可链接到项目内任意文本文件（例如 Markdown 文件）的指定行，

支持鼠标悬停、或 Ctrl+Click 、或快捷键跳转到指定文件的指定行号。

支持在设置页面中进行自定义配置。

Example:/示例：

```java
// @doc file.txt

/* @markdown docs/README.md#L42 */

/**
 * note
 * @see test.md:30
 */
```

-----

Q：为什么会有这样的插件出现？

A：如果你和我一样，是一个三天打鱼两天晒网的独立开发者/独立游戏开发者，那么，一定会遇到一个难题，那就是：自己写下的代码，因为间隔了很长一段时间，已经完全记不得当时这些代码的作用是什么、技术细节是什么、关联的场景有哪些等等。更糟糕的是，虽然当时可能记了一些笔记，但是，笔记却记不得记在了哪里。

然后，自然而然就会有一个做法，就是把笔记以markdown的形式放在工程目录下，比如doc文件夹（或者更直观更便于查找，直接放在代码的同级目录下）。虽然这样做对于一些开源工程早已是常态，但是，这还不够。因为代码可能会出现在工程的各个目录中，但是markdown文件可能只会出现在<b>某一个</b>目录下，此时，怎样让工程代码和<b>这一个</b>markdown建立起关联，是一个问题，至少intellij官方似乎没有意图实现类似的功能（尽管看上去这种实现既不困难也不复杂）。

所以，我就诞生了自己写一个小插件的想法。一开始只是打算自己用，后来感觉这个东西看上去似乎还是有些价值，那么就开源出来，看下能否让这个插件有更大的发展:

以下有些功能是未来的想法，是未来插件的开发目标或者说对我来说是必要的：

[ ] 目前只有代码注释关联到markdown文件的行数，未来考虑能否关联到markdown的大纲标题（实现难度：4星）

[ ] 目前只有代码注释关联到markdown文件，未来考虑通过markdown文件查询所有代码注释的关联，即双向关联（实现难度：4星）
