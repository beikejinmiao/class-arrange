#include<stdio.h>
#include<stdlib.h>
#include<string.h>

#define INIT_NUMBER         -1
#define RETURN_CODE_SUCCESS 0
#define RETURN_CODE_NULL    -1

#define DATA_LINE_LENGTH    25
#define BIG_CLASS_NUM_MAX   30

#define CLASS_ARRANGED              1
#define CLASS_NOT_ARRANGED          0
#define FLAG_LIST_ALL_ARRANGED      1
#define FLAG_LIST_ALL_NOT_ARRANGED  0

#define allocate_space() ((scls *)malloc(sizeof(scls)))


typedef struct SmallCls{
    int id;
    int pid;
    int priority;
    int student_num;
    int is_arranged;
    int x;  //id of lab
    int y;  //id of time
}scls;

typedef struct node{
    scls *data;
    struct node *next;
}scls_node, *scls_list;


int resolve_row(char *row, scls *cls);
int string2int(char *str);
scls_list init_scls_list(scls *head_data);
void insert_scls(scls_list head, scls *data);
void arrange_handle(scls_list *bcls_arr);
void backtrace(scls_list head);
scls_list renew_list_sequence(scls_list head);
int set_time_lab(scls_list head, int time, int lab);
int set_sclslist_all_arranged(scls_list head);
int swap_node_content(scls_list p, scls_list q);
int find_min_num(scls_list head);
int scls_list_sum(scls_list head);
void print_list_info(scls_list head);

const int TimeLab[4][5] = 
{
    {113, 99, 99, 99, 60},
    {113, 99, 99, 99, 60},
    {113, 99, 99, 99, 60},
    {113, 99, 99, 99, 60}
};

int currw = 0;      //实验室当前人数
int bestw = 0;      //实验室最优人数

int restw       = 0;        //实验室剩余容量
int curr_totalw = 0;        //当前实验室总容量

int flag_all_isarranged = FLAG_LIST_ALL_NOT_ARRANGED;

int bcls_num = INIT_NUMBER;

int main(){
    int i = 0;
    FILE *fp;
    char ch, str[DATA_LINE_LENGTH];
    int bcls_id_old = INIT_NUMBER;
    int bcls_id_new = INIT_NUMBER;

    scls *small_class;
    scls_list big_class_arr[BIG_CLASS_NUM_MAX];
    scls_list scls_list_head, p;

    if((fp=fopen("./small_class_info.txt", "rt")) == NULL){
        printf("\nCan't open file!");
        exit(1);
    }  

    i = 0; 
    while(fgets(str, DATA_LINE_LENGTH, fp) != NULL){
        small_class = allocate_space();
        bcls_id_new = resolve_row(str, small_class);
        if(bcls_id_new != bcls_id_old){
            bcls_num++;
            scls_list_head = init_scls_list(small_class);
            big_class_arr[i++] = scls_list_head;
            bcls_id_old = bcls_id_new;
        }else{
            insert_scls(scls_list_head, small_class);
        }
    }
    arrange_handle(big_class_arr);

    for(i=0; i<bcls_num; i++){
        scls_list_head = big_class_arr[i];
        p = scls_list_head;
        print_list_info(p); 
        printf("*****************\n");
    }

    fclose(fp);
    return 0;
}

scls_list init_scls_list(scls *head_data){
    scls_list list;
    list = (scls_list)malloc(sizeof(scls_node));
    list->data = head_data;
    list->next = NULL;
    return list;
}

void insert_scls(scls_list head, scls *data){
    scls_list new_tail;
    scls_list p = head;

    new_tail = (scls_list)malloc(sizeof(scls_node));
    new_tail->data = data;
    new_tail->next = NULL;
    while(p->next != NULL){
        p = p->next;
    }
    p->next = new_tail;
    //return new_tail;
}



int resolve_row(char *row, scls *cls){
    int len = strlen(row);
    int i, tmp;
    int big_class_id = INIT_NUMBER;
    char *val;
    const char *split = "\t";

    row[len-2] = '\0';  //windows newline is '\r\n'
    val = strtok(row, split);
    i = 0;
    while(val != NULL){
        tmp = string2int(val);
        switch(i){
            case 0: cls->id = tmp; break;
            case 1: {
                        cls->pid = tmp; 
                        big_class_id = tmp;
                        break;
                    }
            case 2: cls->priority = tmp; break;
            case 3: cls->student_num = tmp; break;
            case 4: cls->is_arranged = tmp; break;
        }
        i++;
        val = strtok(NULL, split);
    }
    cls->x = INIT_NUMBER;
    cls->y = INIT_NUMBER;

    return big_class_id;
}

int string2int(char *str){
    int len = strlen(str);
    int i;
    int decimal = 1;
    int sum = 0;
    for(i=len-1; i>=0; i--){
        decimal *= 10;
        sum += (str[i]-'0') * decimal; 
    }
    sum = sum / 10;
    return sum;
}


void arrange_handle(scls_list *bcls_arr){
    int i, j;
    int k = 0;
    int min_num = INIT_NUMBER;
    int sum_tmp = 0;

    scls_list head = bcls_arr[0];
    scls_list origin_head = head;
    for(i=0; i<4; i++){
        for(j=0; j<5; j++){

            printf("i=%d, j=%d\n", i, j);
            printf("pid:%d\n", head->data->pid);

            curr_totalw = TimeLab[i][j];
            restw = curr_totalw;
            bestw = INIT_NUMBER + 1;
            currw = INIT_NUMBER + 1;
            //如果实验室能够容纳当前大班剩余所有人
            //则不进行递归回溯，直接将所有小班级所有人全部安排在此实验室
            sum_tmp = scls_list_sum(head);    
            if(sum_tmp <= curr_totalw){
                do{
                    set_sclslist_all_arranged(head);
                    set_time_lab(origin_head, i, j);
                    curr_totalw -= sum_tmp;
                    restw = curr_totalw;

                    k++;
                    head = bcls_arr[k];
                    origin_head = head;
                    sum_tmp = scls_list_sum(head);        
                }while(sum_tmp <= curr_totalw);
            }
            
            if(head != NULL){
                backtrace(head);
                head = renew_list_sequence(head);
            }
            set_time_lab(origin_head, i, j);
            //test

            print_list_info(origin_head);
            printf("==========================================================\n");

            while(flag_all_isarranged == FLAG_LIST_ALL_ARRANGED){
                k++;
                head = bcls_arr[k];
                origin_head = head;
                flag_all_isarranged = FLAG_LIST_ALL_NOT_ARRANGED;

                min_num =  find_min_num(head);
                if(restw >= min_num){                       //*****************大于最小的班级人数************//
                    curr_totalw = restw;
                    bestw = INIT_NUMBER + 1;
                    currw = INIT_NUMBER + 1;
                    backtrace(head);
                    head = renew_list_sequence(head);
                    set_time_lab(origin_head, i, j);
                }
            }

        }
    }
}


//回溯排课
void backtrace(scls_list p){
    //scls_list p = head;
    //到达叶子节点
    if(p == NULL){
        printf("End!\n");
        if(currw > bestw){
            printf("currw > bestw---currw:%d, bestw:%d\n", currw,bestw);
            bestw = currw;
        }
        return;
    }

    printf("*****curr_totalw:%d, restw:%d, currw:%d, bestw:%d*****\n",curr_totalw,restw,currw,bestw);

    restw -= p->data->student_num;
    //搜索左子树
    if(currw + p->data->student_num < curr_totalw){
        p->data->is_arranged = CLASS_ARRANGED;
        currw += p->data->student_num;
        backtrace(p->next);
        currw -= p->data->student_num;
    }
    //搜索右子树
    if(currw + restw > bestw){
        p->data->is_arranged = CLASS_NOT_ARRANGED;
        backtrace(p->next);
    }
    restw += p->data->student_num;
}

//重新将小班排序
//将已排课的小班排在链表头，未排课的下班放在链表尾
scls_list renew_list_sequence(scls_list head){
    if(head == NULL){
        printf("renew_list_sequence(): new head node of list is null!\n");   
        return NULL; 
    }

    if(head->next == NULL){
        flag_all_isarranged = FLAG_LIST_ALL_ARRANGED;
        printf("Change Flag: %d\n", flag_all_isarranged);
        return NULL;
    }

    scls_list new_head = NULL;
    scls_list p = head;
    scls_list q = head;
    //print_list_info(head);              //****************print*****************//
    while((p != NULL) && (q != NULL)){
        while((p != NULL) && (p->data->is_arranged == CLASS_ARRANGED)){
            p = p->next;
            q = p;
        }
        while((q != NULL) && (q->data->is_arranged == CLASS_NOT_ARRANGED)){
            q = q->next;
        }
        if(p != NULL && q !=NULL){
            swap_node_content(p,q);
        } 
    }

    new_head = p;
    if(new_head == NULL){
        flag_all_isarranged = FLAG_LIST_ALL_ARRANGED;
        //printf("Change Flag: %d\n", flag_all_isarranged);
    }

    return new_head;
}


int set_time_lab(scls_list head, int time, int lab){
    if(head == NULL){
        printf("set_time_lab(): head node of list is null!\n");   
        exit(RETURN_CODE_NULL);
    }

    scls_list p = head;
    while(p != NULL){
        if((p->data->is_arranged == CLASS_ARRANGED) && (p->data->x == INIT_NUMBER)){
            p->data->x = time;
            p->data->y = lab;
        }
        p = p->next;
    }

    return RETURN_CODE_SUCCESS;
}

int set_sclslist_all_arranged(scls_list head){
    if(head == NULL){
        printf("set_sclslist_all_arranged(): head node of list is null!\n");   
        return RETURN_CODE_NULL;
    }

    scls_list p = head;
    while(p != NULL){
        p->data->is_arranged = CLASS_ARRANGED;
        p = p->next;
    }

    return RETURN_CODE_SUCCESS;   
}

int swap_node_content(scls_list p, scls_list q){
    if(p == NULL || q == NULL){
        printf("swap_node_content(): swap node can not be null!\n");
        exit(RETURN_CODE_NULL);
    }

    scls_list tmp;
    tmp = (scls_list)malloc(sizeof(scls_node));

    tmp->data = p->data;
    p->data = q->data;
    q->data = tmp->data;

    free(tmp);
    return RETURN_CODE_SUCCESS;
}


int find_min_num(scls_list head){
    if(head == NULL){
        printf("find_min_num(): head node of list is null!\n");
        return RETURN_CODE_NULL;    
    }

    int min = head->data->student_num;
    scls_list p = head;
    while(p != NULL){
        if(p->data->student_num < min){
            min = p->data->student_num;
        }

        p = p->next;
    }

    return min;
}

int scls_list_sum(scls_list head){
    if(head == NULL){
        printf("scls_list_sum): head node of list is null!\n");
        return 0;    
    }

    int sum = 0;
    scls_list p = head;
    while(p != NULL){
        sum += p->data->student_num;
        p = p->next;
    }

    return sum;
}

void print_list_info(scls_list head){
    scls_list p = head;
    while(p != NULL){
        printf("id:%d, pid:%d, priority:%d, num:%d, is_arranged:%d, x:%d, y:%d\n", 
                p->data->id,p->data->pid,p->data->priority,p->data->student_num,p->data->is_arranged,p->data->x,p->data->y);

        p = p->next;
    }

}
