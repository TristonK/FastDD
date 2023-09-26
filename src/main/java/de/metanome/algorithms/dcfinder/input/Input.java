package de.metanome.algorithms.dcfinder.input;

import java.nio.charset.StandardCharsets;
import java.util.*;

import com.csvreader.CsvReader;
import fastdd.Config;
import de.metanome.algorithms.dcfinder.helpers.IndexProvider;

public class Input {

    private final String name;
    private final int colCount;
    private final int rowCount;
    private int longCnt, doubleCnt, stringCnt;
    private final List<ParsedColumn<?>> parsedColumns;

    // 存储输入具体值
    private long[][] longInput;
    private double[][] doubleInput;
    private String[][] stringInput;

    private final IndexProvider<String> providerS;
    private final IndexProvider<Long> providerL;
    private final IndexProvider<Double> providerD;

    private int[][] colIndex;

    public Input(RelationalInput relationalInput) {
        this(relationalInput, -1);
    }

    public Input(RelationalInput relationalInput, int rowLimit) {
        name = relationalInput.relationName();
        providerS = new IndexProvider<>();
        providerL = new IndexProvider<>();
        providerD = new IndexProvider<>();

        longCnt = doubleCnt = stringCnt = 0;
        Column[] columns = readRelationalInputToColumns(relationalInput, rowLimit);
        colCount = columns.length;
        rowCount = colCount > 0 ? columns[0].getLineCount() : 0;

        parsedColumns = buildParsedColumns(columns);
        colIndex = new int[3][Math.max(longCnt, Math.max(doubleCnt, stringCnt))];
        buildInput(parsedColumns);
        System.out.println("[Name]: " + name);
        System.out.println(" [Input] # of Tuples: " + rowCount);
        System.out.println(" [Input] # of Attributes: " + colCount + "(Integer: " + longCnt + "; Double: "+ doubleCnt + "; String: " + stringCnt + " )");

    }

    private Column[] readRelationalInputToColumns(RelationalInput relationalInput, int rowLimit) {
        final int columnCount = relationalInput.numberOfColumns();
        Column[] columns = new Column[columnCount];
        for (int i = 0; i < columnCount; ++i) {
            columns[i] = new Column(relationalInput.relationName(), relationalInput.columnNames[i]);
        }

        int nLine = 0;
        try {
            CsvReader csvReader = new CsvReader(relationalInput.filePath, ',', StandardCharsets.UTF_8);
            csvReader.readHeaders();    // skip the header
            while (csvReader.readRecord()) {
                String[] line = csvReader.getValues();
                for (int i = 0; i < columnCount; ++i) {
                    columns[i].addLine(line[i]);
                }

                ++nLine;
                if (rowLimit > 0 && nLine >= rowLimit) {
                    break;
                }
            }
            csvReader.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return columns;
    }

    private List<ParsedColumn<?>> buildParsedColumns(Column[] columns) {
        List<ParsedColumn<?>> pColumns = new ArrayList<>(colCount);
        Arrays.sort(columns, new Comparator<Column>() {
            @Override
            public int compare(Column o1, Column o2) {
                return o2.getType().compareTo(o1.getType());
            }
        });
        for (int i = 0; i < colCount; i++) {
            Column c = columns[i];
            if (c.getType() == Column.Type.LONG) {
                ParsedColumn<Long> pColumn = new ParsedColumn<>(c.getName(), Long.class, i, providerL);
                pColumns.add(pColumn);
                for (int l = 0; l < c.getLineCount(); ++l) {
                    pColumn.addLine(c.getLong(l));
                }
                longCnt++;
            } else if (c.getType() == Column.Type.NUMERIC) {
                ParsedColumn<Double> pColumn = new ParsedColumn<>(c.getName(), Double.class, i, providerD);
                pColumns.add(pColumn);
                for (int l = 0; l < c.getLineCount(); ++l) {
                    pColumn.addLine(c.getDouble(l));
                }
                doubleCnt++;
            } else if (c.getType() == Column.Type.STRING) {
                ParsedColumn<String> pColumn = new ParsedColumn<>(c.getName(), String.class, i, providerS);
                pColumns.add(pColumn);
                for (int l = 0; l < c.getLineCount(); ++l) {
                    pColumn.addLine(c.getString(l));
                }
                stringCnt++;
            }
        }
        return pColumns;
    }

    private void buildInput(List<ParsedColumn<?>> pColumns){
        longInput = new long[longCnt][rowCount];
        doubleInput = new double[doubleCnt][rowCount];
        stringInput = new String[stringCnt][rowCount];
        int iid = 0, did = 0, sid = 0;
        for (int col = 0; col < colCount; col++) {
            ParsedColumn<?> pColumn = pColumns.get(col);
            if(pColumn.getType() ==String.class){
                for (int row = 0; row < rowCount; ++row){
                    stringInput[sid][row] = (String) pColumn.getValue(row);
                }
                colIndex[2][sid] = col;
                sid++;
            }else if(pColumn.getType()==Double.class){
                for (int row = 0; row < rowCount; ++row){
                    doubleInput[did][row] = (Double)pColumn.getValue(row);
                }
                colIndex[1][did] = col;
                did++;
            } else{
                for (int row = 0; row < rowCount; ++row){
                    longInput[iid][row] =  ((Long) pColumn.getValue(row));
                }
                colIndex[0][iid] = col;
                iid++;
            }
        }
    }

    public int getRowCount() {
        return rowCount;
    }

    public int getColCount() {//新增
        return colCount;
    }

    public long[][] getLongInput() {
        return longInput;
    }

    public ParsedColumn<?>[] getColumns() {
        return parsedColumns.toArray(new ParsedColumn[0]);
    }

    public List<ParsedColumn<?>> getParsedColumns() {
        return parsedColumns;
    }

    public String getName() {
        return name;
    }

    public double[][] getDoubleInput(){
        return this.doubleInput;
    }

    public String[][] getStringInput() {return this.stringInput;}

    /**
     * @param type: 0-int, 1-double, 2-string
     */
    public int getCol(int type, int index){
        return colIndex[type][index];
    }
}
