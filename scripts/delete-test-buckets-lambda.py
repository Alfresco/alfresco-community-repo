import json
import boto3
from botocore.exceptions import ClientError


# This python module is intended for use as a Python 3 AWS lambda function
# Tested in python 3.6 environment
# The AWS role used with this lambda function will need AmazonS3FullAccess and CloudWatchLogsFullAccess permissions
# Tested with role lambda_s3_execution_role in engineering account

# Retrieve bucket's tag set
def get_tagset(bucket):
    try:
        return bucket.Tagging().tag_set
    except ClientError as e:
        return []

# Check if a bucket should be deleted
def tag_matches(bucket):
    for tag in get_tagset(bucket):
        if tag["Key"] == "toDeleteAfterTests" and tag["Value"] == "true"  :
            return True
    return False

def prefix_matches(bucket, prefix):
    if not prefix:
        return True
    if bucket.name.startswith(prefix):
        return True
    return False

# Get a list of buckets to delete
def get_buckets_to_delete(prefix):
    s3 = boto3.resource('s3')
    # Get all buckets matching bucket name prefix
    prefixed_buckets = [bucket for bucket in s3.buckets.all() if prefix_matches(bucket, prefix)]
    # Filter buckets on tag
    tagged_buckets = [bucket for bucket in prefixed_buckets if tag_matches(bucket)]
    return tagged_buckets


# Delete bucket
def delete_bucket(bucket):
    try:
        bucket.objects.all().delete()
        bucket.delete()
        print("Bucket " + bucket.name + " was deleted")
    except ClientError as e:
        print("Failed to delete bucket: " + bucket.name)
        print(e)

# Non-empty buckets are deleted (recursively); failed attempts will be logged.
# The buckets are filtered on the name prefix: "travis-ags-worm-"
def lambda_handler(event, context):

    # Retrieve bucket name prefix option
    prefix = "travis-ags-worm-"

    # Get a list of buckets to delete
    buckets_to_delete = get_buckets_to_delete(prefix)

    # Delete buckets
    print ("Deleting buckets:")
    for bucket in buckets_to_delete :
        print (bucket.name)
        delete_bucket(bucket)

    return {
        'statusCode': 200,
        'body': json.dumps('Done!')
    }

