 //商品类目控制层 
app.controller('itemCatController' ,function($scope,$controller   ,itemCatService,typeTemplateService){
	
	$controller('baseController',{$scope:$scope});//继承
	
    //读取列表数据绑定到表单中  
	$scope.findAll=function(){
		itemCatService.findAll().success(
			function(response){
				$scope.list=response;
			}			
		);
	}    
	
	//分页
	$scope.findPage=function(page,rows){			
		itemCatService.findPage(page,rows).success(
			function(response){
				$scope.list=response.rows;	
				$scope.paginationConf.totalItems=response.total;//更新总记录数
			}			
		);
	}
	
	//查询实体 
	$scope.findOne=function(id){				
		itemCatService.findOne(id).success(
			function(response){
				$scope.entity= response;					
			}
		);				
	}
	
	//保存 
	$scope.save=function(){				
		var serviceObject;//服务层对象  				
		if($scope.entity.id!=null){//如果有ID
			serviceObject=itemCatService.update( $scope.entity ); //修改  
		}else{
			$scope.entity.parentId=$scope.parentId;//赋予上级ID
			serviceObject=itemCatService.add( $scope.entity  );//增加 
		}				
		serviceObject.success(
			function(response){
				if(response.success){
					//重新查询
					$scope.findByParentId($scope.parentId);//重新加载
		        	//$scope.reloadList();//重新加载
				}else{
					alert(response.message);
				}
			}		
		);				
	}
	
	 
	//批量删除 
	$scope.dele=function(){			
		//获取选中的复选框			
		itemCatService.dele( $scope.selectIds ).success(
			function(response){
				if(response.success){
					$scope.findByParentId($scope.parentId);//刷新列表
					//$scope.reloadList();//刷新列表
					$scope.selectIds=[];
				}						
			}		
		);				
	}
	
	$scope.searchEntity={};//定义搜索对象 
	
	//搜索
	$scope.search=function(page,rows){			
		itemCatService.search(page,rows,$scope.searchEntity).success(
			function(response){
				$scope.list=response.rows;	
				$scope.paginationConf.totalItems=response.total;//更新总记录数
			}			
		);
	}

	$scope.parentId=0;//上级ID

	//根据父级ID显示下级列表
	$scope.findByParentId=function(parentId){
		$scope.parentId=parentId;//记住上级ID
		itemCatService.findByParentId(parentId).success(function(response){
				$scope.list=response;
			}
		);
	}

	$scope.grade=1;//默认为1级
	//设置级别，只要传一个值进来就设置为级别，比如传入1，这时的级别就是1级
	$scope.setGrade=function(value){
		$scope.grade=value;
	}
	//读取列表，面包屑导航条，p_entity就是一个变量，传入什么就是什么
	$scope.selectList=function(p_entity){
		if($scope.grade==1){//如果为1级，就只有顶级分类列表，而没有一级分类和二级分类，如：顶级分类列表/null/null
			$scope.entity_1=null;
			$scope.entity_2=null;
		}
		if($scope.grade==2){//如果为2级，一级分类列表就会有数据，二级分类列表就没有数据，如：顶级分类列表/entity_1/null
			$scope.entity_1=p_entity;
			$scope.entity_2=null;
		}
		if($scope.grade==3){//如果为3级，二级分类列表会有数据，不会影响大上面两级，如：顶级分类列表/entity_1/entity_2
			$scope.entity_2=p_entity;
		}
		//每次使用导航条之后，就要重新加载此级分类下的数据
		$scope.findByParentId(p_entity.id);
	}

	$scope.typeTemplateList={data:[]};//模板列表
	//读取模板列表
	$scope.findtypeTemplateList=function(){
		typeTemplateService.selectOptionList().success(
			function(response){
				$scope.typeTemplateList={data:response};
			}
		);
	}

});	