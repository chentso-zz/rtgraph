# RTgraph

Very early release, no customizations, not ready for general use. 


## RTGraphSurfaceView
Subclass of SurfaceView. This is more so for fixed windows without dynamic UI animations (like scrolling). 


## RTGraphView
Subclass of View so dynamic UI does not affect the redraw (scrolling the parent activity won't flash the drawing). Performance is less ideal. 

![RTGraphView Screenshot](https://raw.githubusercontent.com/chentso/rtgraph/master/images/RTGraphView_scap.png)

### Usage

In XML layout, specify a graph control defining the size of the graph

	<tso.chen.rtgraph.RTGraphView
       android:id="@+id/myGraph"
       android:layout_width="match_parent"
       android:layout_height="100dp" />
        

In the Activity:

    // Get the graph view
    RTGraphView myGraph = (RTGraphView) findViewById(R.id.myGraph);
    
    // (Demo) Use this to see some random real-time noise data for testing
    myGraph.setDemoMode(true);
    
    // (Optional) Set the line width
    myGraph.setLineWidth(1.f);
    
    // (Optional) Set the background color
    myGraph.setBackGroundColor(Color.WHITE);
    
    // Start the graphing thread
    myGraph.start();