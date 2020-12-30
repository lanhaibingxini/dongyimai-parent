 //控制层    引入$location内置服务，主要用于angularjs跳转页面传输数据
app.controller('goodsController' ,function($scope,$controller,$location,goodsService,uploadService,itemCatService,typeTemplateService){
	
	$controller('baseController',{$scope:$scope});//继承
	
    //读取列表数据绑定到表单中  
	$scope.findAll=function(){
		goodsService.findAll().success(
			function(response){
				$scope.list=response;
			}			
		);
	}    
	
	//分页
	$scope.findPage=function(page,rows){			
		goodsService.findPage(page,rows).success(
			function(response){
				$scope.list=response.rows;	
				$scope.paginationConf.totalItems=response.total;//更新总记录数
			}			
		);
	}
	
	//查询实体 
	$scope.findOne=function(){
		//跳转页面时，携带参数id跳转
		var id= $location.search()['id'];//获取参数值
		//如果参数为空，就返回为空
		if(id==null){
			return ;
		}
		goodsService.findOne(id).success(function(response){
				$scope.entity= response;
			//向富文本编辑器添加商品介绍
			editor.html($scope.entity.goodsDesc.introduction);
			//显示图片列表
			$scope.entity.goodsDesc.itemImages= JSON.parse($scope.entity.goodsDesc.itemImages);
			//显示扩展属性
			$scope.entity.goodsDesc.customAttributeItems=  JSON.parse($scope.entity.goodsDesc.customAttributeItems);
			//规格
			$scope.entity.goodsDesc.specificationItems=JSON.parse($scope.entity.goodsDesc.specificationItems);
			//SKU列表规格列转换
			for( var i=0;i<$scope.entity.itemList.length;i++ ){
				$scope.entity.itemList[i].spec = JSON.parse( $scope.entity.itemList[i].spec);
			}
		});
	}
	
	//保存 
	$scope.save=function(){
		//提取文本编辑器的值
		$scope.entity.goodsDesc.introduction=editor.html();
		var serviceObject;//服务层对象  				
		if($scope.entity.goods.id!=null){//如果有ID
			serviceObject=goodsService.update( $scope.entity ); //修改  
		}else{
			serviceObject=goodsService.add( $scope.entity  );//增加 
		}				
		serviceObject.success(
			function(response){
				if(response.success){
					alert('保存成功');
					$scope.entity={};
					editor.html("");
					location.href="goods.html";//跳转到商品列表页
				}else{
					alert(response.message);
				}
			}		
		);				
	}
	
	 
	//批量删除 
	$scope.dele=function(){			
		//获取选中的复选框			
		goodsService.dele( $scope.selectIds ).success(
			function(response){
				if(response.success){
					$scope.reloadList();//刷新列表
					$scope.selectIds=[];
				}						
			}		
		);				
	}
	
	$scope.searchEntity={};//定义搜索对象 
	
	//搜索
	$scope.search=function(page,rows){			
		goodsService.search(page,rows,$scope.searchEntity).success(
			function(response){
				$scope.list=response.rows;	
				$scope.paginationConf.totalItems=response.total;//更新总记录数
			}			
		);
	}

	//保存
	$scope.add=function(){
		//从富文本编辑器中取出内容
		$scope.entity.goodsDesc.introduction=editor.html();
		goodsService.add($scope.entity).success(
			function(response){
				if(response.success){
					alert('保存成功');
					//$scope.entity={};
					$scope.entity={ goodsDesc:{itemImages:[],specificationItems:[]}  };
					editor.html('');//清空富文本编辑器
				}else{
					alert(response.message);
				}
			}
		);
	}


	//上传图片
	$scope.uploadFile=function(){
		uploadService.uploadFile().success(function(response) {
			if(response.success){//如果上传成功，取出url
				$scope.image_entity.url=response.message;//设置文件地址
			}else{
				alert(response.message);
			}
		}).error(function() {
			alert("上传发生错误");
		});
	};

	$scope.entity={goods:{isEnableSpec:'0'},goodsDesc:{itemImages:[]}};//定义页面实体结构
	//添加图片列表
	$scope.add_image_entity=function(){
		$scope.entity.goodsDesc.itemImages.push($scope.image_entity);
	}

	//列表中移除图片
	$scope.remove_image_entity=function(index){
		$scope.entity.goodsDesc.itemImages.splice(index,1);
	}

	//读取一级分类
	$scope.selectItemCat1List=function(){
		itemCatService.findByParentId(0).success(
			function(response){
				$scope.itemCat1List=response;
			}
		);
	}

	//读取二级分类
	//$watch方法用于监控某个变量的值，当被监控的值发生变化，就自动执行相应的函数。
	$scope.$watch('entity.goods.category1Id', function(newValue, oldValue) {
		//判断一级分类有选择具体分类值，在去获取二级分类
		if(newValue!=null){
			//根据选择的值，查询二级分类
			itemCatService.findByParentId(newValue).success(
				function(response){
					$scope.itemCat2List=response;
				}
			);
		}
	});

	//读取三级分类
	$scope.$watch('entity.goods.category2Id', function(newValue, oldValue) {
		//判断二级分类有选择具体分类值，在去获取三级分类
		if(newValue!=null){
			//根据选择的值，查询二级分类
			itemCatService.findByParentId(newValue).success(
				function(response){
					$scope.itemCat3List=response;
				}
			);
		}
	});

	//三级分类选择后  读取模板ID
	$scope.$watch('entity.goods.category3Id', function(newValue, oldValue) {
		//判断三级分类被选中，在去获取更新模板id
		if(newValue!=null){
			itemCatService.findOne(newValue).success(
				function(response){
					$scope.entity.goods.typeTemplateId=response.typeId; //更新模板ID
				}
			);
		}
	});

	//模板ID选择后  更新模板对象
	$scope.$watch('entity.goods.typeTemplateId', function(newValue, oldValue) {
		if(newValue){
			typeTemplateService.findOne(newValue).success(
				function(response){
					$scope.typeTemplate=response;//获取类型模板
					$scope.typeTemplate.brandIds= JSON.parse( $scope.typeTemplate.brandIds);//品牌列表
					//如果没有携带参数id
					if ($location.search()['id']==null){
						$scope.entity.goodsDesc.customAttributeItems=JSON.parse( $scope.typeTemplate.customAttributeItems);//扩展属性
					}
				}
			);
			//查询规格列表
			typeTemplateService.findSpecList(newValue).success(
				function(response){
					$scope.specList=response;
				}
			);
		}
	});

	$scope.entity={ goodsDesc:{itemImages:[],specificationItems:[]}  };

	$scope.updateSpecAttribute=function($event,name,value){
		var object= $scope.searchObjectByKey($scope.entity.goodsDesc.specificationItems ,'attributeName', name);
		if(object!=null){
			if($event.target.checked ){
				object.attributeValue.push(value);
			}else{
				//取消勾选
				object.attributeValue.splice( object.attributeValue.indexOf(value ) ,1);//移除选项
				//如果选项都取消了，将此条记录移除
				if(object.attributeValue.length==0){
					$scope.entity.goodsDesc.specificationItems.splice($scope.entity.goodsDesc.specificationItems.indexOf(object),1);
				}
			}
		}else{
			$scope.entity.goodsDesc.specificationItems.push({"attributeName":name,"attributeValue":[value]});
		}
	}

	//[{"attributeName":"网络制式","attributeValue":["移动3G","移动4G"]},{"attributeName":"屏幕尺寸","attributeValue":["6寸","5寸"]}]
	//创建SKU列表
	$scope.createItemList=function(){
		//首先创建一个初始的sku列表
		$scope.entity.itemList=[{spec:{},price:0,num:999,status:'0',isDefault:'0' } ];//初始
		//将规格结果集（包括规格及规格选项）用一个变量接收
		var items=  $scope.entity.goodsDesc.specificationItems;
		//遍历这个结果集，得到每一个规格名称及规格选项
		for(var i=0;i< items.length;i++){
			//将初始的sku列表、规格名称、规格选项作为参数，利用addColumn方法返回一个新的sku列表
			$scope.entity.itemList = addColumn( $scope.entity.itemList,items[i].attributeName,items[i].attributeValue );
		}
	}

	//添加sku列表值
	addColumn=function(list,columnName,conlumnValues){
		//初始化sku列表
		var newList=[];//新的集合
		//遍历传入的sku表集合，获取其中的元素
		for(var i=0;i<list.length;i++){
			//得到的每一行sku表即一个sku对象，用变量oldRow接收
			var oldRow= list[i];
			//由于规格选项也是一个数组，遍历得到每一个规格选项
			for(var j=0;j<conlumnValues.length;j++){
				//JSON.stringify()是将js对象转换成json字符串，而JSON.parse()是将json字符串又转换成对象，经过两次转换，就克隆了一个跟原来一模一样的sku对象
				var newRow= JSON.parse( JSON.stringify( oldRow )  );//深克隆
				//将遍历出来的每一个规格选项用新对象的规格名称接收
				newRow.spec[columnName]=conlumnValues[j];
				//将新对象存入sku列表中
				newList.push(newRow);
			}
		}
		return newList;
	}

	//创建一个数组表示商品审核状态
	$scope.status=['未审核','已审核','审核未通过','关闭'];//商品状态

	//创建一个数组表示查询的分类信息
	$scope.itemCatList=[];//商品分类列表

	//加载商品分类列表
	$scope.findItemCatList=function(){
		itemCatService.findAll().success(function(response){
				for(var i=0;i<response.length;i++){
					//处理查询出来的分类数据，只要id和name
					$scope.itemCatList[response[i].id]=response[i].name;
				}
			}
		);
	}

	//根据规格名称和选项名称返回是否被勾选
	$scope.checkAttributeValue=function(specName,optionName){
		var items= $scope.entity.goodsDesc.specificationItems;
		var object= $scope.searchObjectByKey(items,'attributeName',specName);
		if(object==null){
			return false;
		}else{
			if(object.attributeValue.indexOf(optionName)>=0){
				return true;
			}else{
				return false;
			}
		}
	}

});	