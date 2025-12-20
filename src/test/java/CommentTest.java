/**
 * a @doc ./test.md:10
 *
 * @markdown ./test.md:20
 * @see ./test.md:30
 */
public class CommentTest {

    // 单行注释测试
    //@doc ./test.md:10
    // @markdown ./test.md:20
    //  @see ./test.md:30
    //  @see-doc ./test.md:30

    /*
     * 块注释测试 - 多行
     *@doc ./test.md#10
     * @markdown ./test.md#20L
     *  @see ./test.md#30l
     */

    /** @doc ./test.md#10 **/
    /** @markdown ./test.md#20L **/
    /** @see ./test.md#30l **/

    //不生效的跳转：
    // @doc ./...

}
