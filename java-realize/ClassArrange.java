package flower;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;

public class ClassArrange {
	private static int executeCount = 0;	//记录是大班列表是第几个组合数产生
	
	private static final int INIT_NUMBER = 0;

	private static final String filePath = "C:\\Users\\ljm\\Desktop\\small_class_info.txt";
	private static HashMap<Integer, ArrayList<SmallClass>> bigClassMap = new HashMap<Integer, ArrayList<SmallClass>>();
	private static ArrayList<SmallClass> smallClasses = null;
	private static int smallClassCount 		= INIT_NUMBER; 	//最开始时数组smallClasses中小班的个数
	private static int smallClassCountTmp 	= INIT_NUMBER; 	//当前数组smallClasses中小班的个数
	private static ArrayList<SmallClass> smallClassesResult;

	private static final int CLASS_TIME 			= 4;
	private static final int CLASS_LAB 				= 5;
	private static final int BIG_CLASS_NUMBER_MAX 	= 30;					//大班的最大数量

	private static final int CLASS_ARRANGED 	= 1;
	private static final int CLASS_NOT_ARRANGED = 0;
	
	private static final int ARRAYLIST_CLASS_ALL_NOT_ARRANGED 	= 0;		//数组中的小班未全部被排课
	private static final int ARRAYLIST_CLASS_ALL_ARRANGED 		= 1;		//数组中的小班全部被排课
	private static final int STATURDAY_ALL_LAB_ARRANGED			= 2;		//周六所有实验室排满
	private static final int STATURDAY_ALL_LAB_NOT_ARRANGED		= 3;		//周六所有实验室未排满，有空着未排课的实验室--此情况不可取

	private static int currNum 		= INIT_NUMBER;		//当前实验室被排课人数
	private static int bestNum 		= INIT_NUMBER;		//当前实验室被排课最优人数
	private static int restNum 		= INIT_NUMBER;		//当前未排课小班总人数
	private static int currTotalNum = INIT_NUMBER;		//当前实验室总容量

	private static int bigClassNum 		= INIT_NUMBER;	//实际大班总量
	private static int staBigClassNum 	= INIT_NUMBER;	//周六排课大班数量
	private static int sunBigClassNum 	= INIT_NUMBER;	//周日排课大班数量

	private static int[] indexStaturday = new int[BIG_CLASS_NUMBER_MAX];	//用于保存周六大班组合数
	private static int[] indexSunday 	= new int[BIG_CLASS_NUMBER_MAX];	//用于保存周日大班组合数
	
	private static int[] currResult;	//当前排课解
	private static int[] bestResult;	//当前最优排课解

	int[][] TimeLab = { 
		{ 113, 99, 99, 99, 60 }, 
		{ 113, 99, 99, 99, 60 },
		{ 113, 99, 99, 99, 60 }, 
		{ 113, 99, 99, 99, 60 } 
	};

	/**
	 * 处理数据文件，读取数据，构造数据结构
	 */
	public void fileHandle() {
		int bigClassPreviousId = -1;
		int bigClassNextId = -1;
		String line = "";
		String[] sArray = {};
		SmallClass smallClass = null;
		ArrayList<SmallClass> smallClassList = null;
		try {
			File file = new File(filePath);
			InputStreamReader reader = new InputStreamReader(new FileInputStream(file));
			BufferedReader br = new BufferedReader(reader);
			line = br.readLine();
			while (line != null) {
				sArray = line.split("\t");
				bigClassNextId = Integer.valueOf(sArray[1]);
				smallClass = new SmallClass();
				smallClass.setId(Integer.valueOf(sArray[0]));
				smallClass.setPid(Integer.valueOf(bigClassNextId));
				smallClass.setPriority(Integer.valueOf(sArray[2]));
				smallClass.setStudentNum(Integer.valueOf(sArray[3]));
				smallClass.setIsArranged(Integer.valueOf(sArray[4]));

				if (bigClassNextId != bigClassPreviousId) {
					bigClassNum++;
					smallClassList = new ArrayList<SmallClass>();
					bigClassMap.put(bigClassNextId, smallClassList);
				}
				smallClassList.add(smallClass);

				bigClassPreviousId = bigClassNextId;

				line = br.readLine();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 产生大班组合数
	 * @param m
	 * @param n
	 */
	public void genCombination(int m, int n) {	
		for (int i = m; i >= n; i--) {
			indexStaturday[n] = i;			/*indexStaturday的第一位竟然是0*/
			if (n > 1) {
				genCombination(i - 1, n - 1);
			} else {
				executeCount ++;
				
				initBigClassMap();
				if (arrangeStaturday() == ARRAYLIST_CLASS_ALL_ARRANGED) {
					//周六排课成功，输出周六排课结果
					printStaResult();
					//进入周日排课
					if (arrangeSunday() == ARRAYLIST_CLASS_ALL_ARRANGED) {
						//周日排课成功，输入周日排课结果，并结束程序
						printSunResult();
						System.exit(0);
					}
					
					/*此句仅为测试用，正式版请删除*/
					printSunResult();	//观看周日排课情况【周日未排课成功】
				}
				printStaResult();
			}
		}
	}

	/**
	 * 初始化大班哈希表
	 */
	private void initBigClassMap(){
		ArrayList<SmallClass> smallClassListTmp = null;
		SmallClass smallClassTmp = null;
		for (int key = 1; key <= bigClassNum; key++) {
			smallClassListTmp = bigClassMap.get(key);
			for (int i = 0; i < smallClassListTmp.size(); i++) {
				smallClassTmp = smallClassListTmp.get(i);
				smallClassTmp.setIsArranged(CLASS_NOT_ARRANGED);
				smallClassTmp.setX(-1);
				smallClassTmp.setY(-1);
			}
		}
	}
	
	/**
	 * 周六排课
	 * @return
	 */
	public int arrangeStaturday() {
		smallClasses = new ArrayList<SmallClass>();
		smallClassesResult = new ArrayList<SmallClass>();
		
		if (executeCount == 1491) {
			System.out.println();
		}
		
		for (int i = 1; i <= staBigClassNum; i++) {				/*Warnning:索引值从1开始，注意，有问题*/
			smallClasses.addAll(bigClassMap.get(indexStaturday[i]));
		}
		smallClassCount = smallClasses.size();

		for (int x = 0; x < CLASS_TIME; x++) {
			for (int y = 0; y < CLASS_LAB; y++) {
				currTotalNum = TimeLab[x][y];
				restNum = INIT_NUMBER;
				for (int i = 0; i < smallClasses.size(); i++) {
					restNum += smallClasses.get(i).getStudentNum();
				}
				bestNum = INIT_NUMBER;
				currNum = INIT_NUMBER;
				smallClassCountTmp = smallClasses.size();
				bestResult = new int[smallClassCountTmp]; //初始化结果数组
				currResult = new int[smallClassCountTmp];

				//针对实验室有剩余情况
				//该情况下，所有的班级已经被排课，但是嗨哟空着的实验室
				//对于周六来说，这种情况不可取
				if (smallClassCountTmp == 0) {
					return STATURDAY_ALL_LAB_NOT_ARRANGED;
				}
				
				backtrace(0);
				setTimeLab(x, y);
			}
		}
		
		if (smallClasses.size() == 0) {
			return ARRAYLIST_CLASS_ALL_ARRANGED;
		}

		return ARRAYLIST_CLASS_ALL_NOT_ARRANGED;

	}
	
	/**
	 * 周日排课
	 * @return
	 */
	public int arrangeSunday() {
		if (executeCount == 1491) {
			System.out.println();
		}
		
		int value = -1;
		int[] indexTmp = new int[staBigClassNum];
		int k = 0;
		for (int i = 0; i < indexStaturday.length; i++) {
			value = indexStaturday[i];
			if (value > 0) {
				indexTmp[k] = value;
				k++;
			}
		}
		
		//找出待周日排课的大班ID
		k = 0;
		for (int i = 1; i <= bigClassNum; i++) {
			//如果大班i不在indexStaturday[]中，就添加到indexSunday[]中
			if (!binarySearch(indexTmp, i)) {
				indexSunday[k] = i;
				k++;
			}
		}
		
		smallClasses = new ArrayList<SmallClass>();
		smallClassesResult = new ArrayList<SmallClass>();
		
		for (int i = 0; i < sunBigClassNum; i++) {
			smallClasses.addAll(bigClassMap.get(indexSunday[i]));
		}
		smallClassCount = smallClasses.size();

		for (int x = 0; x < CLASS_TIME; x++) {
			for (int y = 0; y < CLASS_LAB; y++) {
				currTotalNum = TimeLab[x][y];
				restNum = INIT_NUMBER;
				for (int i = 0; i < smallClasses.size(); i++) {
					restNum += smallClasses.get(i).getStudentNum();
				}
				bestNum = INIT_NUMBER;
				currNum = INIT_NUMBER;
				smallClassCountTmp = smallClasses.size();
				bestResult = new int[smallClassCountTmp]; // 初始化结果数组
				currResult = new int[smallClassCountTmp];
				
				//针对实验室有剩余情况
				//该情况下，所有的班级已经被排课，但是嗨哟空着的实验室
				//对于周日来说，这种情况可取
				if (smallClassCountTmp == 0) {
					return ARRAYLIST_CLASS_ALL_ARRANGED;
				}
				
				backtrace(0);
				setTimeLab(x, y);
			}
		}
		
		if (smallClasses.size() == 0) {
			return ARRAYLIST_CLASS_ALL_ARRANGED;
		}

		return ARRAYLIST_CLASS_ALL_NOT_ARRANGED;
	}
	
	/**
	 * 回溯确定最优结果
	 * @param i
	 */
	public void backtrace(int i) {
		//到达叶节点
		if (i > smallClassCountTmp - 1) { // i此时的值=叶节点+1
			if (currNum > bestNum) {
				for (int j = 0; j < smallClassCountTmp; j++) {
            		bestResult[j] = currResult[j];
            		bestNum = currNum;  
				}
				return;
			}
		}

		SmallClass tmpClass = smallClasses.get(i);

		restNum -= tmpClass.getStudentNum();
		//搜索左子树
		if (currNum + tmpClass.getStudentNum() < currTotalNum) {
			currResult[i] = CLASS_ARRANGED;  
			currNum += tmpClass.getStudentNum();
			backtrace(i + 1);
			currNum -= tmpClass.getStudentNum();
		}
		//搜索右子树
		if (currNum + restNum > bestNum) {
			currResult[i] = CLASS_NOT_ARRANGED;  
			backtrace(i + 1);
		}

		restNum += tmpClass.getStudentNum();
	}

	/**
	 * 设置已排课班级的上课时间和地点
	 * @param x	上课时间
	 * @param y	上课地点
	 */
	public void setTimeLab(int x, int y) {		
		SmallClass tmpClass = null;
		for (int i = 0; i < bestResult.length; i++) {
			//System.out.print(bestResult[i] + " ");
			
			if (bestResult[i] == CLASS_ARRANGED) {
				tmpClass = smallClasses.get(i);
				tmpClass.setX(x);
				tmpClass.setY(y);
				tmpClass.setIsArranged(CLASS_ARRANGED);
				smallClassesResult.add(tmpClass);			//添加已排课班级
			}
		}

		//移除已排课班级
		for (int j = 0; j < smallClasses.size(); j++) {
			if (smallClasses.get(j).getIsArranged() == CLASS_ARRANGED) {
				 smallClasses.remove(j);
				 --j;
			}
		}
	}

	/**
	 * 二分查找法
	 * @param array
	 * @param num
	 * @return
	 */
	public boolean binarySearch(int[] array, int num){
		int left = 0; 
		int right = array.length-1;
		int middle = 0;
		
		while(left <= right){
			middle = (left + right) / 2;
			if(array[middle] < num){
				left = middle + 1;
			}
			else if (array[middle] > num) {
				right = middle - 1;
			}
			else {
				return true;
			}
		}
		
		return false;
	}
	

	/**
	 * 打印排课结果
	 * @param list
	 */
	public void printArrayList(ArrayList<SmallClass> list) {
		SmallClass rltClass = null;
		for (int i = 0; i < list.size(); i++) {
			rltClass = list.get(i);
			System.out.print("count:" + ((i<9) ? ("0"+(i+1)) : i+1) + "\t");
			System.out.print("id:" + rltClass.getId() + "\t" + "pid:" + rltClass.getPid() + "\t");
			System.out.print("studentNum:" + rltClass.getStudentNum() + "\t" + "isArranged:" + rltClass.getIsArranged() + "\t");
			System.out.print("x:" + rltClass.getX() + "\t" + "y:" + rltClass.getY());
			System.out.println();
		}
	}
	
	public void printStaResult() {
		System.out.println("第几个组合数：");
		System.out.println(executeCount);
		System.out.println("周六排课的大班列表：");
		for (int j = 1; j <= staBigClassNum; j++) {			/*Warnning:索引值从1开始，注意，有问题*/
			System.out.print(indexStaturday[j] + "\t");
		}
		System.out.println();
		System.out.println("周六排课的小班个数：");
		System.out.println(smallClassCount);
		System.out.println("周六排课结果：");
		System.out.println("序号\t\t" + "小班ID\t\t" + "大班ID\t" + "人数 \t\t" + "是否被排课\t\t" + "上课时间\t" + "上课地点");
		printArrayList(smallClassesResult);
	}

	public void printSunResult() {
		System.out.println("第几个组合数：");
		System.out.println(executeCount);
		System.out.println("周日排课的大班列表：");
		for (int j = 0; j < sunBigClassNum; j++) {
			System.out.print(indexSunday[j] + "\t");
		}
		System.out.println();
		System.out.println("周日排课的小班个数：");
		System.out.println(smallClassCount);
		System.out.println("周日排课结果：");
		System.out.println("序号\t\t" + "小班ID\t\t" + "大班ID\t" + "人数 \t\t" + "是否被排课\t\t" + "上课时间\t" + "上课地点");
		printArrayList(smallClassesResult);
	}
	
	public static void main(String[] args) {
		ClassArrange arrange = new ClassArrange();
		arrange.fileHandle();
		staBigClassNum = bigClassNum / 2 + bigClassNum % 2; // 周六排课的大班数量
		sunBigClassNum = bigClassNum - staBigClassNum;
		arrange.genCombination(bigClassNum, staBigClassNum);
		//排课失败
		System.out.println("排课失败！");
	}
}
