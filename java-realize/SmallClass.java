package flower;

public class SmallClass {
	private int id;				//小班id
	private int pid;			//小班所在大班id
	private int priority;		//排课优先级
	private int studentNum;		//小班学生人数
	private int isArranged;		//小班是否被排课
	private int x = -1;			//上课时间
	private int y = -1;			//上课地点
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public int getPid() {
		return pid;
	}
	public void setPid(int pid) {
		this.pid = pid;
	}
	public int getPriority() {
		return priority;
	}
	public void setPriority(int priority) {
		this.priority = priority;
	}
	public int getStudentNum() {
		return studentNum;
	}
	public void setStudentNum(int studentNum) {
		this.studentNum = studentNum;
	}
	public int getIsArranged() {
		return isArranged;
	}
	public void setIsArranged(int isArranged) {
		this.isArranged = isArranged;
	}
	public int getX() {
		return x;
	}
	public void setX(int x) {
		this.x = x;
	}
	public int getY() {
		return y;
	}
	public void setY(int y) {
		this.y = y;
	}
}
