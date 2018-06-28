package entity;

import java.io.Serializable;
import java.util.List;

//分页插件框架,需要的返回值封装成对象
public class PageResult<T> implements Serializable {

	private Long total;// 总记录数
	private List<T> rows;// 当前页结果

	public Long getTotal() {
		return total;
	}

	public void setTotal(Long total) {
		this.total = total;
	}

	public PageResult(Long total, List<T> rows) {
		super();
		this.total = total;
		this.rows = rows;
	}

	public List<T> getRows() {
		return rows;
	}

	public void setRows(List<T> rows) {
		this.rows = rows;
	}

}
