[[ ! -f "LICENSE" ]] && echo "run the dockerbuild script from the project root directory like this: ./docker/dockerbuild.sh" && exit -1

docker build \
--format docker \
-t roboquant/jupyter:latest \
-f docker/Dockerfile .