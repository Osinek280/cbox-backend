#!/bin/bash
awslocal s3 ls s3://cbox-bucket || awslocal s3 mb s3://cbox-bucket
##!/bin/bash
#awslocal s3 mb s3://cbox-bucket
