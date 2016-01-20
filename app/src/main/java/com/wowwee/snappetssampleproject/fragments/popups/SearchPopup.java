package com.wowwee.snappetssampleproject.fragments.popups;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.qwerjk.better_text.MagicTextView;
import com.wowwee.bluetoothrobotcontrollib.snappets.SnapPets;
import com.wowwee.bluetoothrobotcontrollib.snappets.SnapPetsCommandValues;
import com.wowwee.snappetssampleproject.R;
import com.wowwee.snappetssampleproject.enums.CAMERA_MODE;
import com.wowwee.snappetssampleproject.fragments.BaseFragment;
import com.wowwee.snappetssampleproject.fragments.CameraFragment;
import com.wowwee.snappetssampleproject.snappethelper.ConnectSnappetCallback;
import com.wowwee.snappetssampleproject.snappethelper.FoundSnappetCallback;
import com.wowwee.snappetssampleproject.snappethelper.SnappetsHelper;
import com.wowwee.snappetssampleproject.util.CameraInstance;
import com.wowwee.snappetssampleproject.util.FragmentHelper;
import com.wowwee.snappetssampleproject.util.SharedSettingsHelper;
import com.wowwee.snappetssampleproject.util.SnappetsScreenReference;
import com.wowwee.snappetssampleproject.util.ViewHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class SearchPopup extends BaseFragment {

    private CameraInstance.Callback cameraCallback;

    private List<SnapPets> snappetList = new ArrayList<>();

    private FoundSnappetCallback foundSnappetCallback;

    private BaseAdapter snappetAdapter;
    private MagicTextView titleText;
    private View connectingLayout;
    private View searchLayout;
    private View buyLayout;
    private ProgressBar connectingProgressbar;
    private Handler handler;
    private boolean stopHadler;

    private Timer timer;
    private ConnectionTimerTask connectionTimerTask;
    private final int CONNECTION_TIMEOUT = 30000;

    public SearchPopup() {
        initializeFragment(R.layout.fragment_popup_search, new SnappetsScreenReference());
    }

    public void setCameraCallback(CameraInstance.Callback cameraCallback) {
        this.cameraCallback = cameraCallback;
    }

    @Override
    public void _onCreateView(View view) {
        setupSearchLayout(view);
        setupBuyLayout(view);
        setupCloseButton(view);
        setupRefreshButton(view);
        setupAdapterListView(view);
        setupSearchPets();
        setupConnectingLayout(view);
        setupTimer();
    }

    private List<View> getAllViews(View v) {
        if (!(v instanceof ViewGroup) || ((ViewGroup) v).getChildCount() == 0) // It's a leaf
        {
            List<View> r = new ArrayList<View>();
            r.add(v);
            return r;
        } else {
            List<View> list = new ArrayList<View>();
            list.add(v); // If it's an internal node add itself
            int children = ((ViewGroup) v).getChildCount();
            for (int i = 0; i < children; ++i) {
                list.addAll(getAllViews(((ViewGroup) v).getChildAt(i)));
            }
            return list;
        }
    }

    private void setupBuyLayout(View view) {
        buyLayout = view.findViewById(R.id.buySnapPetLayout);
//        ViewHelper.showViewWithAnimation(getActivity(), buyLayout);
        buyLayout.setVisibility(View.VISIBLE);
        ViewHelper.setButtonTouchListener(view.findViewById(R.id.btn_popup_buy_close));
        view.findViewById(R.id.btn_popup_buy_close).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                closeButtonClicked();
            }
        });

        view.findViewById(R.id.buySnapPet).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                buyButtonClicked();
            }
        });

        view.findViewById(R.id.snapPetLogo).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                buyButtonClicked();
            }
        });
    }

    private void buyButtonClicked() {
        Intent urlIntent = new Intent(Intent.ACTION_VIEW);
        //TODO: load url from resource
        urlIntent.setData(Uri.parse("http://www.wowwee.com/"));
        startActivity(urlIntent);

        //TO REMOVE

    }


    private void setupSearchLayout(View view) {
        searchLayout = view.findViewById(R.id.searchLayout);
        ViewHelper.showViewWithAnimation(getActivity(), searchLayout);
        titleText = (MagicTextView) view.findViewById(R.id.txt_title);
    }

    private void setupConnectingLayout(View view) {
        connectingLayout = view.findViewById(R.id.connectingLayout);
        connectingProgressbar = (ProgressBar)view.findViewById(R.id.connectingProgressbar);
        connectingLayout.setVisibility(View.INVISIBLE);
        connectingProgressbar.setVisibility(View.INVISIBLE);
    }

    private void setupRefreshButton(View rootView) {
        View refreshButton = rootView.findViewById(R.id.btn_popup_refresh);
        ViewHelper.setButtonTouchListener(refreshButton);
        refreshButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                refreshSearch();
            }
        });
    }

    private void refreshSearch() {
        SnappetsHelper.getInstance().startSearch(foundSnappetCallback);
    }

    private void setupSearchPets() {
        foundSnappetCallback = createFoundSnappetsCallback();
        subscribeOnSearchResults();
    }

    private void setupTimer() {
        timer = new Timer();
        connectionTimerTask = new ConnectionTimerTask();
    }

    private FoundSnappetCallback createFoundSnappetsCallback() {
        return new FoundSnappetCallback() {
            @Override
            public void callback(final List<SnapPets> snappets) {
                SearchPopup.this.snappetList = snappets;
                try {
                    if (SearchPopup.this.getActivity() != null && connectingLayout.getVisibility() != View.VISIBLE) {
                        SearchPopup.this.getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                snappetAdapter.notifyDataSetChanged();
                            }
                        });
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
    }

    private void subscribeOnSearchResults() {
        if (SnappetsHelper.getInstance().isSupported()) {
            if (SnappetsHelper.getInstance().prepare(this)) {
                //start to search snappets
                SnappetsHelper.getInstance().setContext(context);
                SnappetsHelper.getInstance().startSearch(foundSnappetCallback);
            }
        } else {
            Toast.makeText(context, getString(R.string.doesnt_support), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        CameraFragment.isSearchingSnappets = true;
        stopHadler = false;
        updateReserch();
    }

    private void updateReserch() {
        if (handler == null) {
            handler = new Handler();
        }

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                refreshSearch();
                if (!stopHadler)
                    updateReserch();
            }
        }, 5000);
    }

    @Override
    public void onPause() {
        super.onPause();
        CameraFragment.isSearchingSnappets = false;
        stopHadler = true;
    }

    private void setupAdapterListView(View view) {
        snappetAdapter = getDevicesAdapter();
        final ListView snappetGridView = (ListView) view.findViewById(R.id.grid_snappet);
        snappetGridView.setAdapter(snappetAdapter);

    }

    private BaseAdapter getDevicesAdapter() {
        return new BaseAdapter() {
            @Override
            public int getCount() {
                if (snappetList.size() > 0) {
                    return snappetList.size();
                } else {
                    if (titleText.getText().toString().equals(getString(R.string.connecting) + " ...")) {
                        return 0;
                    }
                    return 1;
                }
            }

            @Override
            public Object getItem(int position) {
                return null;
            }

            @Override
            public long getItemId(int position) {
                return position;
            }

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View grid = null;

                if (convertView == null) {
                    grid = ((LayoutInflater) context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.grid_snapppet_result, parent, false);
                } else
                    grid = convertView;

                final TextView labelText = (TextView) grid.findViewById(R.id.txt_label);

                //handle SnapPet
                if (snappetList.size() > 0) {
                    buyLayout.setVisibility(View.INVISIBLE);
                    searchLayout.setVisibility(View.VISIBLE);
                    final SnapPets snapPet = snappetList.get(position);

                    //TODO (David/Katy) load related icon from SnapPet
                    switch (snapPet.getColorID()) {
                        case SnapPetsCommandValues.kSnapPetsColorAquaBlue:
                            labelText.setTextColor(getResources().getColor(R.color.pet1));
                            break;
                        case SnapPetsCommandValues.kSnapPetsColorOrange:
                            labelText.setTextColor(getResources().getColor(R.color.pet2));
                            break;
                        case SnapPetsCommandValues.kSnapPetsColorPink:
                            labelText.setTextColor(getResources().getColor(R.color.pet3));
                            break;
                        case SnapPetsCommandValues.kSnapPetsColorBlue:
                            labelText.setTextColor(getResources().getColor(R.color.pet4));
                            break;
                        case SnapPetsCommandValues.kSnapPetsColorSliver:
                            labelText.setTextColor(getResources().getColor(R.color.pet1));
                            break;
                        default:
                            labelText.setTextColor(getResources().getColor(R.color.pet3));
                            break;
                    }

                    //load related name from SnapPet
                    labelText.setText(snapPet.getName());

                    labelText.setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            //try to connect snappet
                            buyLayout.setVisibility(View.INVISIBLE);
                            connectingLayout.setVisibility(View.VISIBLE);
                            connectingProgressbar.setVisibility(View.VISIBLE);
                            searchLayout.setVisibility(View.INVISIBLE);
//                            snappetList.clear();
//                            snappetAdapter.notifyDataSetChanged();notifyDataSetChanged

                            // Start count connection timeout
                            timer.schedule(connectionTimerTask, CONNECTION_TIMEOUT);

                            SnappetsHelper.getInstance().setContext(SearchPopup.this.getActivity());
                            SnappetsHelper.getInstance().connect(snapPet, new ConnectSnappetCallback() {
                                @Override
                                public void connected(final SnapPets snapPet) {
                                    ((Activity) context).runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            connectionTimerTask.cancel();
                                            SnappetsHelper.getInstance().isPinCodeChecked = false;
                                            CameraFragment.cameraMode = CAMERA_MODE.SNAPPET;
                                            cameraCallback.init();
                                            FragmentHelper.popBackStack(getFragmentManager());
                                        }
                                    });
                                }

                                @Override
                                public void disconnected(SnapPets snapPet) {
                                }

                                @Override
                                public void receiveImageTotalSize(int size) {
                                }

                                @Override
                                public void receivedImagePieceSize(int size) {
                                }

                                @Override
                                public void receivedImageBuffer(byte[] buffer) {
                                }

                                @Override
                                public void didPressedButton() {
                                }

                                @Override
                                public void receivedPhotoCount(int count) {
                                    // TODO Auto-generated method stub

                                }

                                @Override
                                public void didDeletePhoto(int id) {
                                    // TODO Auto-generated method stub

                                }
                            });
                        }
                    });
                }
                //handle default one "Buy a SnapPet"
                else {
                    buyLayout.setVisibility(View.VISIBLE);
                    searchLayout.setVisibility(View.INVISIBLE);
                }

                return grid;
            }
        };
    }

    private void setupCloseButton(View rootView) {
        View closeButton = rootView.findViewById(R.id.btn_popup_close);
        ViewHelper.setButtonTouchListener(closeButton);
        closeButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                closeButtonClicked();
            }
        });
    }

    private void closeButtonClicked() {
        SnappetsHelper.getInstance().stopSearch();
        FragmentHelper.popBackStack(getFragmentManager());
    }

    @Override
    public boolean allowBackPress() {
        SnappetsHelper.getInstance().stopSearch();
        return true;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == SnappetsHelper.REQUEST_CODE_BT_ENABLE) {
            if (resultCode == Activity.RESULT_CANCELED) {
                Toast.makeText(context, getString(R.string.deny_bt_enable), Toast.LENGTH_SHORT).show();
            } else if (resultCode == Activity.RESULT_OK) {
                //start to search snappets
                SnappetsHelper.getInstance().startSearch(foundSnappetCallback);
            }
        }
    }

    class ConnectionTimerTask extends TimerTask {
        @Override
        public void run() {
            try {
                if (SearchPopup.this.getActivity() != null && connectingLayout.getVisibility() == View.VISIBLE) {
                    SearchPopup.this.getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (SnappetsHelper.getInstance().getConnectedSnappet() != null) {
                                SnappetsHelper.getInstance().isPinCodeChecked = false;
                                CameraFragment.cameraMode = CAMERA_MODE.SNAPPET;
                                cameraCallback.init();
                            }
                            FragmentHelper.popBackStack(getFragmentManager());
                        }
                    });
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
