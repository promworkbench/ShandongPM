package CrossOrgMining;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JComponent;

import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.contexts.uitopia.annotations.Visualizer;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.plugins.graphviz.dot.Dot;
import org.processmining.plugins.graphviz.dot.Dot.GraphDirection;
import org.processmining.plugins.graphviz.dot.DotCluster;
import org.processmining.plugins.graphviz.dot.DotEdge;
import org.processmining.plugins.graphviz.dot.DotNode;
import org.processmining.plugins.graphviz.visualisation.DotPanel;


public class VisualizeCrossOrganizationBusinessProcessModel {
	@Plugin(name = "Visualize Cross-organization Business Process Model", 
	returnLabels = { "Dot visualization" }, 
	returnTypes = { JComponent.class }, 
	parameterLabels = { "Cross-organization Model" }, 
	userAccessible = true)
	@Visualizer
	@UITopiaVariant(affiliation = "TU/e", author = "Cong Liu", email = "c.liu.3@tue.nl OR liucongchina@163.com")	
	@PluginVariant(variantLabel = "Visualize Cross-organization Business Process Model", 
		requiredParameterLabels = {0})// it needs one input parameter
	
	public JComponent visualizeCrossorganizationModel(PluginContext context, CrossOrganizationBusinessProcessModel crossOrgModel)
	{	
		Dot dot = new Dot();
		dot.setDirection(GraphDirection.leftRight);//topDown
//		dot.setOption("label", "Cross-organization Business Process Model");
		dot.setOption("fontsize", "24");		  
		
		// it records all activity (transition) nodes in the intra-organization model. 
		//它记录了组织内部模型中的所有活动（transition）节点。
		HashSet<DotNode> tDotNodeSet = new HashSet<>();
		HashSet<String> resourceStringSet = new HashSet<>();
		HashSet<String> Act = new HashSet<>();
		HashMap<String, HashSet<OrgActivity>> inteAct = new HashMap<String, HashSet<OrgActivity>>();
		HashSet<OrgActivity> orgAct = new  HashSet<OrgActivity>();
//		HashMap<String, OrgActivity> inteAct = new HashMap<String, OrgActivity>();
		// for each organization, we create a cluster and then visualize its pn 
		//对于每个组织,我们创建一个集群,然后将其可视化
		Set<String> orgSet = crossOrgModel.getAllOrganizations();
		
		for(String org:orgSet)	{
			//create a cluster for each organization.
			//为每个组织创建一个集群。
			DotCluster orgCluster =dot.addCluster();
			orgCluster.setOption("label", org); // organization name, as the label
			orgCluster.setOption("penwidth", "2.0"); // width of the organization border组织边界的宽度
			orgCluster.setOption("fontsize", "18");
			orgCluster.setOption("color","black");  
			orgCluster.setOption("style","dashed");
			
			//visualize the pn
			//可视化pn
			VisualizeHPNandInteraction2Dot.visualizePN2Dot(crossOrgModel.getOrganizationModel(org), orgCluster, tDotNodeSet);	
		}
		
		//for each interaction, we add a place to connect the corresponding transitions.
		//the source or target of an interaction may involve mutliple activities, attributed from choice behavior.
		//对于每个交互,我们添加一个位置来连接相应的过渡。
		//交互的源或目标可能涉及由选择行为引起的多重活动。
		HashSet<CrossOrganizationInteraction> interactions = crossOrgModel.getAllInteractions();
		List<String> resource = new ArrayList<String>();
		for(CrossOrganizationInteraction inte: interactions)
		{		
//			System.out.println("inte:-------------"+inte);
			/*********去掉特殊字符[]*************************************************/
			 String regEx="[`~!@#$%^&*()+=|{}':;',\\[\\].<>/?~！@#￥%……&*（）——+|{}【】‘；：”“’。，、？]";
			 Pattern p=Pattern.compile(regEx);
			 Matcher m=p.matcher(inte.getResource());
			 String aa = "";
			/***************************************************************/
			String str = m.replaceAll(aa).trim();//inte.getResource();
//			System.out.println("inte:"+inte);			
/***
     资源连接：
      HashMap(key, value): 将资源名称作为key; 与这个资源连接的活动[label, org] 作为value			 
 */
			if(str.charAt(0) == 'r') { ///这里是找到资源的连接 ///////////////后面的是另加的		资源是str, 此时key是str		
//				System.out.println("inte.getSourceActivities()):"+inte.getSourceActivities());
//				System.out.println("11orgAct:"+orgAct);
				
					for(OrgActivity sourceA: inte.getSourceActivities()) {
						String reslabel = Pattern.compile(regEx).matcher(inte.getResource()).replaceAll(aa).trim();
//						System.out.println("key:"+inteAct.get(reslabel));
//						System.out.println("key1:"+reslabel);
//						System.out.println("inteAct*******************:"+inteAct);
//						System.out.println("inteAct*111******************:"+inteAct.keySet());	
						if(!inteAct.keySet().contains(reslabel)) {
							inteAct.put(reslabel, new HashSet<OrgActivity>());
						}

//						System.out.println("key22:"+reslabel);
//						System.out.println("inteAct*2222******************:"+inteAct.keySet());	
						
						inteAct.get(reslabel).add(sourceA);
					}						
//				System.out.println("inteAct*******************:"+inteAct.keySet());	
//				System.out.println("inteActvalue*******************:"+inteAct.get("r2"));	
	
			}
			
			else {
				//add a circle //添加一个圆圈
				//the label of message place is the combination of source activity and target activity //消息位置的标签是源活动和目标活动的组合
				MessageDotPlace messageDot= new MessageDotPlace(str);
				dot.addNode(messageDot);	
				//find the source to message connections //找到消息连接的源
				for(DotNode tDot: tDotNodeSet)	{
					for(OrgActivity sourceA: inte.getSourceActivities()) {
						if(tDot.getLabel().equals(sourceA.getActivity())) {
						//	tDot.getLabel().
							//messageDot.setForeground(Color.RED);//[color = red];
							//add an arc from tDot to messageDot
						    //将tDot的弧添加到messageDot
						    DotEdge edge = dot.addEdge(tDot, messageDot);
						    edge.setOption("style", "dashed");
						    edge.setOption("color", "blue");// edge color
						    edge.setOption("penwidth", "1.5");
						    }
						}
					//find the message to target connections
				    //找到消息到目标连接
					for(OrgActivity targetA: inte.getTargetActivities()) {
						if(tDot.getLabel().equals(targetA.getActivity())) {
							//add an arc from messageDot to tDot
						    //从messageDot向tDot添加弧
						    DotEdge edge = dot.addEdge(messageDot, tDot);
						    edge.setOption("style", "dashed");
						    edge.setOption("color", "blue");// edge color
						    edge.setOption("penwidth", "1.5");
						 }
					}
				}
			}
		}//inte
/***
	     资源连接：
	     1、HashMap(key, value): 将资源名称作为key; 与这个资源连接的活动[label, org] 作为value	;
	     2、将HashMap中存的值按照key依次取出, 作为一个set;
	     3、set中活动的label与变迁点集合中比较，相同则添加边  
 */		
		Set set = inteAct.keySet();
		 
		for(Iterator iter = set.iterator(); iter.hasNext();) {
			String key = (String)iter.next();   //key: r1,r2
			HashSet<OrgActivity> actvalue = inteAct.get(key);
			ResourceDotPlace rmessageDot= new ResourceDotPlace(key);	
			dot.addNode(rmessageDot);
			for(DotNode tDot: tDotNodeSet){
				for (OrgActivity sourceA : actvalue ) {
					if(tDot.getLabel().equals(sourceA.getActivity())) {
						DotEdge edge1 = dot.addEdge(tDot, rmessageDot);
						DotEdge edge2 = dot.addEdge(rmessageDot, tDot);
						edge1.setOption("style", "dashed");
						edge1.setOption("color", "purple");// edge color
						edge1.setOption("penwidth", "2");
						edge2.setOption("style", "dashed");
						edge2.setOption("color", "purple");// edge color //maroon
						edge2.setOption("penwidth", "2");
					}
		        }
			}
			//for() {}
			}
		
		return new DotPanel(dot);
	}
	
	
	//inner class for message/interaction place dot
	//消息/交互位置点的内部类
	private static class MessageDotPlace extends DotNode {
			public MessageDotPlace(String messageInfor) {
				super(messageInfor, null);
				setOption("shape", "circle"); //doublecircle
				//setOption("shape", "circle");
				setOption("style", "dashed");
				setOption("color", "blue");
				setOption("penwidth", "1.5");
//				setOption(this.getLabel(), "blue");
//				this.getLabel();
//				setOption("pencolor", "red");
				setOption("fontcolor", "blue"); //圈内字体的颜色
//				System.out.println("1:"+this.getLabel()); //this.Label9.ForeColor = "red" 字体色
			}
	}// inner class for place dot

	
	/***********************资源类的点****************/
	private static class ResourceDotPlace extends DotNode {
		public ResourceDotPlace(String messageInfor) {
			super(messageInfor, null);
			setOption("shape", "doublecircle");
			//setOption("shape", "circle");
			setOption("style", "filled");
			setOption("fillcolor", "white");
			setOption("color", "purple"); //purple,  
			setOption("penwidth", "1.5");
			setOption("fontcolor", "purple");
			//setOption("fillcolor", "grey");
		}
	
}// inner class for place dot
	
}
