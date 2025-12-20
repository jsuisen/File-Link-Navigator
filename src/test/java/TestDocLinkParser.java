import com.saysawgames.filelinknavigator.DocLinkParser;

import java.util.List;

public class TestDocLinkParser {
    /**
     * 示例使用方法
     */
    public static void main(String[] args) {
        String commentText = """
            /**
             * a @doc: ./test.md:10
             *
             * @doc: ./test.md:30
             */
            public class CommentTest {

                // 单行注释测试
                // @doc: ./test.md:5

                /* 块注释测试 - 单行
                 * @doc: ./test.md:10
                 */

                /*
                 * 块注释测试 - 多行
                 * @doc: test.md:15
                 * @markdown: ./test.md:20
                 * @see-doc: ./test.md:25
                 */

                /** @doc: ./test.md:30 **/
                /** @markdown: ./test.md:30 **/

                /**
                 * haha
                 *
                 * @doc: ./test.md:30
                 */
                void testMethod() {
                    // 方法内单行注释
                    // @doc: ./test.md:40

                    /* 方法内块注释
                     * @doc: ./test.md:45
                     */
                }

                //不生效的跳转：
                // @doc: ./...

            }
            """;

        List<DocLinkParser.DocLinkMatch> matches = DocLinkParser.parseDocLinks(commentText);

        System.out.println("找到 " + matches.size() + " 个文档链接：");
        for (DocLinkParser.DocLinkMatch match : matches) {
            System.out.println("-------");
            System.out.println(match.filePath);
            System.out.println(match.lineNumber);
            System.out.println(match.start);
            System.out.println(match.end);
//            System.out.println("  " + match);
//            System.out.println("    完整匹配: \"" + match.fullMatch + "\"");
        }

        /*System.out.println("--------------");

        Matcher matcher = DOC_LINK_PATTERN.matcher(commentText);
        while (matcher.find()) {
            String filePath = matcher.group(1);
            String lineNumberString = matcher.group(2);
            int start = matcher.start();
            int end = matcher.end();
            System.out.println("-------");
            System.out.println(filePath);
            System.out.println(lineNumberString);
            System.out.println(start);
            System.out.println(end);
        }*/
    }
}
