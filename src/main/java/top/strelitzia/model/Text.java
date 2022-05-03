package top.strelitzia.model;

import top.strelitzia.util.FormatStringUtil;

/**
 * @author strelitzia
 * @Date 2022/05/03 14:38
 **/
public class Text {
    //字符串原文
    private String s;
    //按行切分后数组
    private String[] text;
    //最长的一行字符串
    private String maxRow;
    //字符行数
    private int rowsNum;

    public Text(String s) throws Exception {
        this.s = s;

        //二维数组，用于制表符对齐
        String[][] text2d;

        //初步切分，最终将制表符对齐后再赋值给text
        String[] rows = s.split("\n");

        //获取最长数组长度
        int maxWidth = 0;
        int height = rows.length;
        for (int i = 0; i < height; i++) {
            int length = rows[i].split("\t").length;
            if (maxWidth < length) {
                maxWidth = length;
            }
        }

        //给二维数组赋值
        text2d = new String[height][maxWidth];
        for (int i = 0; i < height; i++) {
            String[] row = rows[i].split("\t");
            for (int j = 0; j < row.length; j++) {
                text2d[i][j] = row[j];
            }
        }

        //制表符对齐
        int[] widths = new int[maxWidth];//每列的最长宽度
        for (int i = 0; i < maxWidth; i++) {
            widths[i] = 0;
        }
        for (int i = 0; i < height; i++) {
            if (rows[i].split("\t").length != 1) {
                //只有一列的（行首行尾描述）不进行统计
                for (int j = 0; j < maxWidth; j++) {
                    int length = 0;
                    if (text2d[i][j] != null) {
                        length = text2d[i][j].length();
                    }
                    if (widths[j] < length) {
                        widths[j] = length;
                    }
                }
            }
        }
        String[] newRows = new String[height];
        //制表符对齐后赋值
        for (int i = 0; i < height; i++) {
            if (rows[i].split("\t").length != 1) {
                newRows[i] = "";
                for (int j = 0; j < maxWidth; j++) {
                    newRows[i] += FormatStringUtil.strAppendStr(text2d[i][j], widths[j]);
                }
            } else {
                newRows[i] = rows[i];
            }
        }
        this.text = newRows;

        this.rowsNum = text.length;
        this.maxRow = text[0];
        for (int i = 1; i < rowsNum; i++) {
            if (this.maxRow.length() < text[i].length()) {
                this.maxRow = text[i];
            }
        }
    }

    public int getRowsNum() {
        return rowsNum;
    }

    public void setRowsNum(int rowsNum) {
        this.rowsNum = rowsNum;
    }

    public String getS() {
        return s;
    }

    public void setS(String s) {
        this.s = s;
    }

    public String[] getText() {
        return text;
    }

    public void setText(String[] text) {
        this.text = text;
    }

    public String getMaxRow() {
        return maxRow;
    }

    public void setMaxRow(String maxRow) {
        this.maxRow = maxRow;
    }

}
