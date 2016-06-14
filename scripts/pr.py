# Plot itemset precision-recall
import matplotlib.pyplot as plt
from matplotlib import rc
import numpy as np

rc('ps', fonttype=42)
rc('pdf', fonttype=42)

rc('xtick', labelsize=16) 
rc('ytick', labelsize=16) 

def main():
    
    path = '/afs/inf.ed.ac.uk/user/j/jfowkes/Code/Sequences/PrecisionRecall/Background/'
    probname = 'Background'    
    cols = ['b','g','m','r']
    prefixes = ['ISM','SQS','GoKrimp','BIDE']
    
    for prefix in prefixes:
    
        precision, recall = readdata(open(path+prefix+'_'+probname+'_pr.txt'))
	col = cols[prefixes.index(prefix)]
        
        # Calculate interpolated precision
        pt_recall = np.arange(0,1.1,0.1)
        interp_precision = [pinterp(zip(precision,recall),r) for r in pt_recall]
        plotfigpr(interp_precision,pt_recall,prefix,col,1)
        
    plt.figure(1)   
    plt.legend(prefixes,'lower right')
    plt.show()

# Interpolate precision
def pinterp(prarray,recall):

    m = [p for (p,r) in prarray if r >= recall]
    if(len(m)==0):
        return np.nan
    else:
        return max(m) 

def plotfigpr(precision,recall,name,col,figno):

    # sort
    ind = np.array(recall).argsort()
    r_d = np.array(recall)[ind]
    p_d = np.array(precision)[ind]

    # zorder
    zo = 5	
    if name == 'SQS':
	zo = 10	

    plt.figure(figno)
    plt.hold(True)
    plt.plot(r_d,p_d,'.-',color=col,linewidth=2,markersize=12,clip_on=False,zorder=zo)
    plt.xlabel('Recall',fontsize=16)
    plt.ylabel('Precision',fontsize=16)
    plt.xlim([0,1])
    plt.ylim([0,1])
    plt.grid(True)

def readdata(fl):
   
    for line in fl:
      if 'Precision' in line:
	pre = line.strip().split(': ')[1].replace('[','').replace(']','').split(', ')
      if 'Recall' in line:
	rec = line.strip().split(': ')[1].replace('[','').replace(']','').split(', ')

    return (map(float,pre),map(float,rec))

main()
