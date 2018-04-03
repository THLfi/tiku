package fi.thl.pivot.model;

import java.util.List;

/**
 * 
 * Provides a representation of an iterable pivoted table that is constructed
 * from Hydra fact set.
 * 
 * @author aleksiyrttiaho
 *
 */
public interface Pivot extends Iterable<PivotCell> {
    

    /**
     * Lists all column levels and nodes on each level
     * 
     * @return
     */
    List<PivotLevel> getColumns();

    /**
     * Lists all column levels and nodes on each level
     * 
     * @return
     */
    List<PivotLevel> getRows();

    /**
     * Return the number of columns after filters have been applied
     * 
     * @return number of columns
     */
    int getColumnCount();

    /**
     * Return the number of rows after filters have been applied
     * 
     * @return number of rows
     */
    int getRowCount();

    /**
     * Returns the table cell at the given location
     * 
     * @param row
     *            row index of the cell
     * @param column
     *            column index of the cell
     */
    PivotCell getCellAt(int row, int column);

    /**
     * Returns the header node of column at the given header level
     * 
     * @param level
     *            header level
     * @param column
     *            column number in table
     * @return header node
     */
    IDimensionNode getColumnAt(int level, int column);

    /**
     * Returns the header node of row at the given header level
     * 
     * @param level
     *            header level
     * @param row
     *            row number in table
     * @return header node
     */
    IDimensionNode getRowAt(int level, int row);

    boolean isFirstColumn(int column);
    
    boolean isColumn(int column, int targetColumn);
    
    int getColumnNumber(int column);

    void filterCellAt(int row, int column);

}
