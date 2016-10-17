//
//  LBF.cpp
//  myopencv
//
//  Created by lequan on 1/24/15.
//  Copyright (c) 2015 lequan. All rights reserved.
//

#include "LBF.h"
#include "LBFRegressor.h"
#include "LBF_api.h"
#include "android/log.h"
#define LOG_TAG "FaceAlignment_Native"
#define LOGD(...) ((void)__android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__))

using namespace std;
using namespace cv;

// parameters




string modelPath ="";
string dataPath = "./../../Datasets/";
string cascadeName = "haarcascade_frontalface_alt.xml";

void ReadGlobalParamFromFile(string path){
    cout << "Loading GlobalParam..." << endl;
    LOGD("Loading GlobalParam...");
    ifstream fin;
    fin.open(path.c_str());
    if (fin.is_open()){
        LOGD("fin.is_open");
    } else{
        LOGD("fin.is_open = false");
    }
    fin >> global_params.bagging_overlap;
    fin >> global_params.max_numtrees;
    fin >> global_params.max_depth;
    fin >> global_params.max_numthreshs;
    fin >> global_params.landmark_num;
    fin >> global_params.initial_num;
    fin >> global_params.max_numstage;
    
    for (int i = 0; i< global_params.max_numstage; i++){
        fin >> global_params.max_radio_radius[i];
    }
    
    for (int i = 0; i < global_params.max_numstage; i++){
        fin >> global_params.max_numfeats[i];
    }
    cout << "Loading GlobalParam end"<<endl;
    LOGD("Loading GlobalParam end");
    fin.close();
}

