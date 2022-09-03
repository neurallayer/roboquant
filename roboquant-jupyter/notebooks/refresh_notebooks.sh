#!/bin/bash

#
# Copyright 2020-2022 Neural Layer
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     https://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#

[[ ! -f "metric_charts.ipynb" ]] && echo "run the script from the notebooks directory" && exit 1

# First clean all the outputs in the notebooks
jupyter nbconvert --clear-output --inplace *.ipynb

# Now regenerate the outputs
jupyter nbconvert --execute --inplace *.ipynb
