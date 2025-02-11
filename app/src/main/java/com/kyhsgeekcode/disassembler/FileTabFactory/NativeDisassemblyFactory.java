package com.kyhsgeekcode.disassembler.FileTabFactory;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

import com.kyhsgeekcode.disassembler.AbstractFile;
import com.kyhsgeekcode.disassembler.ColorHelper;
import com.kyhsgeekcode.disassembler.DisasmClickListener;
import com.kyhsgeekcode.disassembler.DisasmListViewAdapter;
import com.kyhsgeekcode.disassembler.MainActivity;
import com.kyhsgeekcode.disassembler.R;
import com.kyhsgeekcode.disassembler.RawFile;
import com.kyhsgeekcode.disassembler.TabType;

import java.io.IOException;

import capstone.Capstone;
import nl.lxtreme.binutils.elf.MachineType;

import static com.kyhsgeekcode.disassembler.MainActivity.CS_ARCH_ALL;
import static com.kyhsgeekcode.disassembler.MainActivity.CS_ARCH_MAX;

public class NativeDisassemblyFactory extends FileTabContentFactory {
    private static final String TAG = "NativeDisasmFactory";
    public NativeDisassemblyFactory(Context context) {
        super(context);
    }

    @Override
    public View createTabContent(String tag) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View root = inflater.inflate(R.layout.disasmtab, null);
        ListView listview = root.findViewById(R.id.disasmTabListview);
        DisasmListViewAdapter adapter = null;
        try {
            final AbstractFile file = AbstractFile.createInstance(tag);
            ((MainActivity) context).setParsedFile(file);
            if(file instanceof RawFile)
                throw new IOException("Raw file not supported now. Maybe wrong adapter called");
            adapter = new DisasmListViewAdapter(file, ColorHelper.getInstance(), (MainActivity) context);

            Log.v(TAG,"");
            MachineType type = file.getMachineType();//elf.header.machineType;
            int[] archs = MainActivity.getArchitecture(type);
            int arch = archs[0];
            int mode = 0;
            if (archs.length == 2)
                mode = archs[1];
            if (arch == CS_ARCH_MAX || arch == CS_ARCH_ALL) {
                //Toast.makeText(this, "Maybe this program don't support this machine:" + type.name(), Toast.LENGTH_SHORT).show();
            } else {
                int err;
                if ((err = MainActivity.Open(arch,/*CS_MODE_LITTLE_ENDIAN =*/ mode)) != Capstone.CS_ERR_OK)/*new DisasmIterator(null, null, null, null, 0).CSoption(cs.CS_OPT_MODE, arch))*/ {
                    Log.e(TAG, "setmode type=" + type.name() + " err=" + err + "arch" + arch + "mode=" + mode);
                    //Toast.makeText(this, "failed to set architecture" + err + "arch=" + arch, Toast.LENGTH_SHORT).show();
                } else {
                    //Toast.makeText(this, "MachineType=" + type.name() + " arch=" + arch, Toast.LENGTH_SHORT).show();
                }
            }
            adapter.LoadMore(0, file.getCodeVirtAddr());
        } catch (IOException e) {
            Log.e(TAG,"Error creating adapter",e);
            Toast.makeText(context,"Failed to parse a file.",Toast.LENGTH_SHORT).show();
            return root;
        }
        listview.setAdapter(adapter);
        listview.setOnItemClickListener(new DisasmClickListener((MainActivity) context));
        listview.setOnScrollListener(adapter);
        return root;
    }

    @Override
    public void setType(String absolutePath, TabType type) {
        super.setType(absolutePath, type);
    }
}
