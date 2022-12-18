#!/bin/bash
#SBATCH --ntasks=1
#SBATCH --cpus-per-task=28
#SBATCH --time=120:00:00
#SBATCH -p standard
#SBATCH --output=job_output/out
#SBATCH --error=job_output/error
#SBATCH --mail-type=ALL
#SBATCH --mail-user=sak2km@virginia.edu
#SBATCH -A hcdm

module load anaconda/5.2.0-py2.7
python scripts/SIGIR2019.py --data_sets local_Webscope_C14_Set1 --click_models inf nav per --log_folder ~/../../../../scratch/sak2km/l2r_data/log_folder --average_folder ~/../../../../scratch/sak2km/l2r_data/outdir/average --output_folder ~/../../../../scratch/sak2km/l2r_data/outdir/fullruns/ --n_runs 5 --n_proc 10 --n_impr 10000