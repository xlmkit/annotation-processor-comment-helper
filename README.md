# annotation-processor-comment-helper
将java注释自动转化成properties文件，如果注释里是脚本或者是sql，你觉得可以用来做什么？

## 作者用途:dao层自动生成
- 有点类似mybatis的mapper.xml和mapper接口
- 优点在于mapper.xml与mapper合成一个dao接口，更方便开发与阅读
###  1.使用@GenerateCommentFile
class com.xlmkit.demo.dao.CollectionDao
``` java
@Model(Collection.class)
@GenerateCommentFile
public interface CollectionDao {
	/**
	 *
	 * <pre>
	 ------------------------------------------------------------------------
	 FROM
	   T
	 WHERE
	   id = { id }
	 * </pre>
	 */
	@HQL
	Collection findById(long id);
	/**
	 * <pre>
	 * SELECT COUNT(*) -- COUNT SQL
	 * SELECT * -- LIST SQL
	 *
	 * FROM
	 * T WHERE
	 * createUserId = {userId}
	 * AND status=1
	 * ORDER BY createTime DESC -- LIST SQL
	 *
	 * </pre>
	 */
	Page<JSONObject> page(long userId, String keyword_type, String keyword, PageRequest page);

}
```
### 2.注解器主动为此类生成properties文件
生成/target/com/xlmkit/demo/dao/CollectionDao.comment.properties
``` properties
##  
##   <pre>
##  	 ------------------------------------------------------------------------
##  	 FROM
##  	   T
##  	 WHERE
##  	   id = { id }
##   </pre>
findById##long=CiA8cHJlPgoJIC0tLS0tLS0tLS0tLS0tLS0tLS0tLS0tLS0tLS0tLS0tLS0tLS0tLS0tLS0tLS0tLS0tLS0tLS0tLS0tLS0tLS0tLS0tLS0tLQoJIEZST00KCSAgIFQKCSBXSEVSRQoJICAgaWQgPSB7IGlkIH0KIDwvcHJlPgo=
parameter.names#findById##long=id,
##   <pre>
##   SELECT COUNT(*) -- COUNT SQL
##   SELECT * -- LIST SQL
##   
##   FROM
##   T WHERE
##   createUserId = {userId}
##   AND status=1
##   ORDER BY createTime DESC -- LIST SQL
##   
##   </pre>
page##long#java.lang.String#java.lang.String#org.springframework.data.domain.PageRequest=IDxwcmU+CiBTRUxFQ1QgQ09VTlQoKikgLS0gQ09VTlQgU1FMCiBTRUxFQ1QgKiAtLSBMSVNUIFNRTAogCiBGUk9NCiBUIFdIRVJFCiBjcmVhdGVVc2VySWQgPSB7dXNlcklkfQogQU5EIHN0YXR1cz0xCiBPUkRFUiBCWSBjcmVhdGVUaW1lIERFU0MgLS0gTElTVCBTUUwKIAogPC9wcmU+Cg==
parameter.names#page##long#java.lang.String#java.lang.String#org.springframework.data.domain.PageRequest=userId,keyword_type,keyword,page,

```
### 以下几步不在注解器范围内，需要自行实现

- 通过spring为每个dao接口生成实现类
- daoimpl通过读取xxx.comment.properties中的语句操作数据库
- 根据方法返回类型采用不用的执行方式
- 根据方法注释执行不用语法活着其他数据源
- 在注释内添加不同标或者魔法变量
  - 如${username}：username占位符
  - 如$text{username}：直接在sql注入username(此时应该防止sql注入)
  - 如 -- LIST SQL:只在分页是查询内容append进去
  - 如 -- COUNT SQL:只在分页查询总数append进去
  - 如 -- check(条件):仅当添加满足才append到sql
    
### 感谢大家的阅读