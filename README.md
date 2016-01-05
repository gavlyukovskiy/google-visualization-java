This fork was created because we wanted to change behavior of writing response message.
In our case it was packing into zip our csv reports if it's larger than 1MB.

So we created our implementation of ResponseWriter and put it into DataSourceServlet.

API changes:

- All usage of static methods of DataSourceHelper and ResponseWriter should be re-written
to DataSourceHelper.getInstance() and ResponseWriter.getInstance().

- DataSourceServlet is used DataSourceHelper.getInstance() by default.
You are abble to change it by calling DataSourceHelper#setDataSourceHelper(DataSourceHelper) with your implementation.
