// Copyright 2009 Google Inc.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.visualization.datasource;

import java.util.List;

import javax.servlet.http.HttpServletResponse;

import com.google.common.collect.Lists;
import com.google.visualization.datasource.base.DataSourceException;
import com.google.visualization.datasource.base.DataSourceParameters;
import com.google.visualization.datasource.base.ReasonType;
import com.google.visualization.datasource.base.ResponseStatus;
import com.google.visualization.datasource.base.StatusType;
import com.google.visualization.datasource.datatable.ColumnDescription;
import com.google.visualization.datasource.datatable.DataTable;
import com.google.visualization.datasource.datatable.TableCell;
import com.google.visualization.datasource.datatable.TableRow;
import com.google.visualization.datasource.datatable.value.NumberValue;
import com.google.visualization.datasource.datatable.value.TextValue;
import com.google.visualization.datasource.datatable.value.ValueType;
import com.google.visualization.datasource.render.JsonRenderer;
import junit.framework.TestCase;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.eq;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

/**
 * Unit test for ResponseWriter.
 *
 * @author Nimrod T.
 */
public class ResponseWriterTest extends TestCase {

  private ResponseWriter responseWriter = ResponseWriter.getInstance();

  private DataTable getTestDataTable() throws DataSourceException {
    DataTable dataTable = new DataTable();
    ColumnDescription c0 = new ColumnDescription("A", ValueType.TEXT, "col0");
    ColumnDescription c1 = new ColumnDescription("B", ValueType.NUMBER, "col1");
    ColumnDescription c2 = new ColumnDescription("C", ValueType.BOOLEAN, "col2");

    dataTable.addColumn(c0);
    dataTable.addColumn(c1);
    dataTable.addColumn(c2);

    List<TableRow> rows = Lists.newArrayList();

    TableRow row = new TableRow();
    row.addCell(new TableCell("aaa"));
    row.addCell(new TableCell(new NumberValue(222), "222"));
    row.addCell(new TableCell(false));
    rows.add(row);

    row = new TableRow();
    row.addCell(new TableCell(""));
    row.addCell(new TableCell(111));
    row.addCell(new TableCell(true));
    rows.add(row);

    row = new TableRow();
    row.addCell(new TableCell((new TextValue("bbb")), "bbb"));
    row.addCell(new TableCell(333));
    row.addCell(new TableCell(true));
    rows.add(row);

    row = new TableRow();
    row.addCell(new TableCell("ddd"));
    row.addCell(new TableCell(222));
    row.addCell(new TableCell(false));
    rows.add(row);
    dataTable.addRows(rows);

    return dataTable;
  }

  public void testSetServletResponseJson() throws DataSourceException {
    // Basic test 1.
    DataTable data = getTestDataTable();
    DataSourceParameters dsParamsJson = 
        new DataSourceParameters("responseHandler:babylon;out:json");
    ResponseStatus responseStatus = new ResponseStatus(StatusType.OK, null, null);

    String expected = "{\"version\":\"0.6\",\"status\":\"ok\","
        + "\"sig\":\"2087475733\",\"table\":"
        + "{\"cols\":[{\"id\":\"A\",\"label\":\"col0\",\"type\":\"string\",\"pattern\":\"\"},"
        + "{\"id\":\"B\",\"label\":\"col1\",\"type\":\"number\",\"pattern\":\"\"},"
        + "{\"id\":\"C\",\"label\":\"col2\",\"type\":\"boolean\",\"pattern\":\"\"}],"
        + "\"rows\":[{\"c\":[{\"v\":\"aaa\"},{\"v\":222.0,\"f\":\"222\"},{\"v\":false}]},"
        + "{\"c\":[{\"v\":\"\"},{\"v\":111.0},{\"v\":true}]},"
        + "{\"c\":[{\"v\":\"bbb\"},{\"v\":333.0},{\"v\":true}]},"
        + "{\"c\":[{\"v\":\"ddd\"},{\"v\":222.0},{\"v\":false}]}]}}";

    assertEquals(expected, JsonRenderer.renderJsonResponse(
        dsParamsJson, responseStatus, data).toString());
    
    DataSourceParameters dsParamsJsonP = 
        new DataSourceParameters("responseHandler:babylon;out:jsonp");
    assertEquals("babylon(" + expected + ");", JsonRenderer.renderJsonResponse(
        dsParamsJsonP, responseStatus, data).toString());

    // Basic test 2.
    data = getTestDataTable();
    dsParamsJson = new DataSourceParameters("reqId:90210;responseHandler:babylon;");
    responseStatus = new ResponseStatus(StatusType.OK, null, null);

    expected = "{\"version\":\"0.6\",\"reqId\":\"90210\",\"status\":\"ok\","
        + "\"sig\":\"2087475733\",\"table\":"
        + "{\"cols\":[{\"id\":\"A\",\"label\":\"col0\",\"type\":\"string\",\"pattern\":\"\"},"
        + "{\"id\":\"B\",\"label\":\"col1\",\"type\":\"number\",\"pattern\":\"\"},"
        + "{\"id\":\"C\",\"label\":\"col2\",\"type\":\"boolean\",\"pattern\":\"\"}],"
        + "\"rows\":[{\"c\":[{\"v\":\"aaa\"},{\"v\":222.0,\"f\":\"222\"},{\"v\":false}]},"
        + "{\"c\":[{\"v\":\"\"},{\"v\":111.0},{\"v\":true}]},"
        + "{\"c\":[{\"v\":\"bbb\"},{\"v\":333.0},{\"v\":true}]},"
        + "{\"c\":[{\"v\":\"ddd\"},{\"v\":222.0},{\"v\":false}]}]}}";

    assertEquals(expected, JsonRenderer.renderJsonResponse(
        dsParamsJson, responseStatus, data).toString());
    
    dsParamsJsonP = new DataSourceParameters("reqId:90210;responseHandler:babylon;out:jsonp");
    assertEquals("babylon(" + expected + ");", JsonRenderer.renderJsonResponse(
        dsParamsJsonP, responseStatus, data).toString());
  }

  public void testGenerateJsonResponseError() throws DataSourceException {
    DataTable data = getTestDataTable();
    DataSourceParameters dsParamsJson =
        new DataSourceParameters("reqId:90210;responseHandler:babylon;");
    ResponseStatus responseStatus = new ResponseStatus(
        StatusType.ERROR,
        ReasonType.INTERNAL_ERROR,
        "this is me not you why it is that not knowing me cave man");

    String expected = "{\"version\":\"0.6\",\"reqId\":\"90210\",\"status\":\"error\",\"errors\":"
        + "[{\"reason\":\"internal_error\",\"message\":\"Internal error\",\"detailed_message\":"
        + "\"this is me not you why it is that not knowing me cave man\"}]}";
    assertEquals(
        expected,
        JsonRenderer.renderJsonResponse(dsParamsJson, responseStatus, data).toString());
    
    DataSourceParameters dsParamsJsonP =
        new DataSourceParameters("reqId:90210;responseHandler:babylon;out:jsonp");
    assertEquals(
        "babylon(" + expected + ");",
        JsonRenderer.renderJsonResponse(dsParamsJsonP, responseStatus, data).toString());
  }
  
  public void testCSVResponse() throws DataSourceException {
    final String csvContentType = "text/csv; charset=UTF-8";
    final String csvheaderName = "Content-Disposition";
    final String csvheaderValue = "attachment; filename=testFile.csv";
    
    DataSourceParameters dsParamsCSV = 
      new DataSourceParameters("outFileName:testFile;out:csv");
    
    HttpServletResponse mockHttpServletResponse = createMock(HttpServletResponse.class);
    mockHttpServletResponse.setContentType(eq(csvContentType));
    mockHttpServletResponse.setHeader(eq(csvheaderName), eq(csvheaderValue));
    expectLastCall();
    
    replay(mockHttpServletResponse);
    responseWriter.setServletResponseCSV(dsParamsCSV, mockHttpServletResponse);
    verify(mockHttpServletResponse);
  }
  
  public void testTSVExcelResponse() throws DataSourceException {
    final String tsvExcelContentType = "text/csv; charset=UTF-16LE";
    final String headerName = "Content-Disposition";
    final String headerValue = "attachment; filename=testFile.xls";
    
    DataSourceParameters dsParamsTsvExcel = 
      new DataSourceParameters("outFileName:testFile.xls;out:tsv_excel");
    
    HttpServletResponse mockHttpServletResponse = createMock(HttpServletResponse.class);
    mockHttpServletResponse.setContentType(eq(tsvExcelContentType));
    mockHttpServletResponse.setHeader(eq(headerName), eq(headerValue));
    expectLastCall();
    
    replay(mockHttpServletResponse);
    responseWriter.setServletResponseTSVExcel(dsParamsTsvExcel, mockHttpServletResponse);
    verify(mockHttpServletResponse);
  }
  
  public void testHTMLResponseContentType() {
    final String HtmlContentType = "text/html; charset=UTF-8";
    HttpServletResponse mockHttpServletResponse = createMock(HttpServletResponse.class);
    mockHttpServletResponse.setContentType(eq(HtmlContentType));
    expectLastCall();
    
    replay(mockHttpServletResponse);
    responseWriter.setServletResponseHTML(mockHttpServletResponse);
    verify(mockHttpServletResponse);
  }
  
  public void testJsonResponseContentType() {
    final String jsonContentType = "application/json; charset=UTF-8";
    HttpServletResponse mockHttpServletResponse = createMock(HttpServletResponse.class);
    mockHttpServletResponse.setContentType(eq(jsonContentType));
    expectLastCall();
    
    replay(mockHttpServletResponse);
    responseWriter.setServletResponseJSON(mockHttpServletResponse);
    verify(mockHttpServletResponse);
  }
  
  public void testJsonpResponseContentType() {
    final String jsonpContentType = "text/javascript; charset=UTF-8";
    HttpServletResponse mockHttpServletResponse = createMock(HttpServletResponse.class);
    mockHttpServletResponse.setContentType(eq(jsonpContentType));
    expectLastCall();
    
    replay(mockHttpServletResponse);
    responseWriter.setServletResponseJSONP(mockHttpServletResponse);
    verify(mockHttpServletResponse);
  }
}
